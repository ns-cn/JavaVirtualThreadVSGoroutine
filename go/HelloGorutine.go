package main

import (
	"fmt"
	"time"
)

func main() {

	go func() {
		fmt.Println("hello goroutine")
	}()

	var hello = func() {
		fmt.Println("hello gorutine!")
	}
	go hello()
	go hello()
	time.Sleep(1 * time.Second)
}
