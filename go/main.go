package main

import (
	"fmt"
	"github.com/spf13/cobra"
	"time"
)

var threads = 1000

var RootCmd = cobra.Command{
	Run: func(cmd *cobra.Command, args []string) {
		startTime := time.Now() // 开始时间
		var inUse = 0
		var finish = 0
		var maxInUse = 0
		var start = make(chan int, 100)
		var end = make(chan int, 100)
		for i := 0; i < threads; i++ {
			go func() {
				start <- 1
				time.Sleep(10)
				end <- 1
			}()
		}
		var finished = false
		for {
			if !finished {
				select {
				case <-start:
					inUse++
					if inUse > maxInUse {
						maxInUse = inUse
					}
				case <-end:
					inUse--
					finish++
					finished = finish >= threads
				}
			} else {
				break
			}
		}
		duration := time.Since(startTime) // 从开始到当前所消耗的时间
		fmt.Printf("isVirtual: %s\tfinal: %.0fs | %d\tmaxInUse: %d\n", "Y", duration.Seconds(), duration.Nanoseconds(), maxInUse)
	},
}

func main() {
	fmt.Println("-----------------GO Routine-----------------")
	RootCmd.Flags().IntVar(&threads, "threads", 1000, "线程数量")
	err := RootCmd.Execute()
	if err != nil {
		return
	}
}
