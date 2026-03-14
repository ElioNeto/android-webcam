package main

import (
	"flag"
	"log"
	"os"
	"os/signal"
	"syscall"

	"github.com/elioneto/android-webcam/desktop/internal/server"
	"github.com/elioneto/android-webcam/desktop/internal/sink"
)

func main() {
	port := flag.Int("port", 8888, "TCP signaling port")
	output := flag.String("output", "/dev/video10", "v4l2loopback device path")
	codec := flag.String("codec", "h265", "Video codec: h264 or h265")
	width := flag.Int("width", 1920, "Frame width")
	height := flag.Int("height", 1080, "Frame height")
	fps := flag.Int("fps", 30, "Frames per second")
	flag.Parse()

	log.Printf("Android Webcam Desktop starting: port=%d output=%s codec=%s %dx%d@%dfps",
		*port, *output, *codec, *width, *height, *fps)

	cameraSink, err := sink.NewV4L2Sink(*output, *width, *height)
	if err != nil {
		log.Fatalf("failed to open camera sink: %v", err)
	}
	defer cameraSink.Close()

	srv, err := server.NewSignalingServer(*port, cameraSink, server.Config{
		Codec:  *codec,
		Width:  *width,
		Height: *height,
		FPS:    *fps,
	})
	if err != nil {
		log.Fatalf("failed to start signaling server: %v", err)
	}
	defer srv.Close()

	log.Printf("Listening for Android connection on TCP :%d ...", *port)

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down.")
}
