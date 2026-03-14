// Package sink abstracts the camera output destination.
package sink

import (
	"fmt"
	"os"
)

// CameraSink is the interface for pushing decoded video frames.
type CameraSink interface {
	// WriteFrame receives a raw I420/YUV frame.
	WriteFrame(data []byte) error
	Close() error
}

// V4L2Sink writes raw frames to a v4l2loopback device (Linux).
// The device must be configured with the matching format beforehand via v4l2-ctl.
type V4L2Sink struct {
	f      *os.File
	width  int
	height int
}

// NewV4L2Sink opens the v4l2loopback device for writing.
func NewV4L2Sink(devicePath string, width, height int) (*V4L2Sink, error) {
	f, err := os.OpenFile(devicePath, os.O_WRONLY, 0600)
	if err != nil {
		return nil, fmt.Errorf("open v4l2 device %s: %w", devicePath, err)
	}
	return &V4L2Sink{f: f, width: width, height: height}, nil
}

func (s *V4L2Sink) WriteFrame(data []byte) error {
	_, err := s.f.Write(data)
	return err
}

func (s *V4L2Sink) Close() error { return s.f.Close() }
