package main

import (
	"fmt"
	"time"
)

func singleGoroutineTest() {

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
