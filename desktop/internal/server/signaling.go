// Package server handles WebRTC signaling over a lightweight TCP JSON channel.
// Protocol:
//
//	{"type": "offer",  "sdp": "..."}
//	{"type": "answer", "sdp": "..."}
//	{"type": "candidate", "candidate": "...", "sdpMid": "...", "sdpMLineIndex": 0}
package server

import (
	"encoding/json"
	"fmt"
	"log"
	"net"

	"github.com/pion/webrtc/v4"

	"github.com/elioneto/android-webcam/desktop/internal/sink"
)

// Config holds stream parameters negotiated during signaling.
type Config struct {
	Codec  string
	Width  int
	Height int
	FPS    int
}

// SignalingServer listens for the Android phone to connect and
// orchestrates the WebRTC offer/answer handshake.
type SignalingServer struct {
	listener net.Listener
	sink     sink.CameraSink
	cfg      Config
}

// NewSignalingServer creates and starts the TCP listener.
func NewSignalingServer(port int, s sink.CameraSink, cfg Config) (*SignalingServer, error) {
	l, err := net.Listen("tcp", fmt.Sprintf(":%d", port))
	if err != nil {
		return nil, err
	}
	srv := &SignalingServer{listener: l, sink: s, cfg: cfg}
	go srv.acceptLoop()
	return srv, nil
}

func (s *SignalingServer) Close() { _ = s.listener.Close() }

func (s *SignalingServer) acceptLoop() {
	for {
		conn, err := s.listener.Accept()
		if err != nil {
			return
		}
		log.Printf("Android connected from %s", conn.RemoteAddr())
		go s.handleConnection(conn)
	}
}

type sigMsg struct {
	Type         string `json:"type"`
	SDP          string `json:"sdp,omitempty"`
	Candidate    string `json:"candidate,omitempty"`
	SDPMid       string `json:"sdpMid,omitempty"`
	SDPMLineIdx  int    `json:"sdpMLineIndex,omitempty"`
}

func (s *SignalingServer) handleConnection(conn net.Conn) {
	defer conn.Close()

	// Configure WebRTC for low latency: disable NACK-based retransmit,
	// enable RTCP feedback for congestion control (TWCC).
	settingEngine := webrtc.SettingEngine{}
	settingEngine.SetSRTPProtectionProfiles(webrtc.SRTP_AES128_CM_HMAC_SHA1_80)

	mediaEngine := &webrtc.MediaEngine{}
	if err := mediaEngine.RegisterDefaultCodecs(); err != nil {
		log.Printf("failed to register codecs: %v", err)
		return
	}

	api := webrtc.NewAPI(
		webrtc.WithMediaEngine(mediaEngine),
		webrtc.WithSettingEngine(settingEngine),
	)

	pc, err := api.NewPeerConnection(webrtc.Configuration{
		ICEServers: []webrtc.ICEServer{},  // LAN only - no STUN needed
	})
	if err != nil {
		log.Printf("WebRTC peer connection error: %v", err)
		return
	}
	defer pc.Close()

	// Track handler: decode and push frames to the camera sink.
	pc.OnTrack(func(track *webrtc.TrackRemote, receiver *webrtc.RTPReceiver) {
		log.Printf("Received track: %s / %s", track.Kind(), track.Codec().MimeType)
		go readRTPTrack(track, s.sink)
	})

	pc.OnICECandidate(func(c *webrtc.ICECandidate) {
		if c == nil { return }
		msg := sigMsg{
			Type:      "candidate",
			Candidate: c.ToJSON().Candidate,
		}
		_ = json.NewEncoder(conn).Encode(msg)
	})

	decoder := json.NewDecoder(conn)
	for {
		var msg sigMsg
		if err := decoder.Decode(&msg); err != nil {
			return
		}
		switch msg.Type {
		case "offer":
			if err := pc.SetRemoteDescription(webrtc.SessionDescription{
				Type: webrtc.SDPTypeOffer, SDP: msg.SDP,
			}); err != nil {
				log.Printf("SetRemoteDescription: %v", err)
				return
			}
			answer, err := pc.CreateAnswer(nil)
			if err != nil {
				log.Printf("CreateAnswer: %v", err)
				return
			}
			if err := pc.SetLocalDescription(answer); err != nil {
				log.Printf("SetLocalDescription: %v", err)
				return
			}
			_ = json.NewEncoder(conn).Encode(sigMsg{Type: "answer", SDP: answer.SDP})
		case "candidate":
			_ = pc.AddICECandidate(webrtc.ICECandidateInit{Candidate: msg.Candidate})
		}
	}
}

// readRTPTrack reads RTP packets from the WebRTC track and forwards
// decoded frames to the camera sink.
// TODO: plug in GStreamer pipeline for H.265 hardware decode.
func readRTPTrack(track *webrtc.TrackRemote, s sink.CameraSink) {
	buf := make([]byte, 1500)
	for {
		_, _, err := track.Read(buf)
		if err != nil {
			return
		}
		// TODO: reassemble NAL units, hand off to GStreamer appsrc
	}
}
