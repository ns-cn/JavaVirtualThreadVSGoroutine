package main

import (
	"fmt"
	"github.com/spf13/cobra"
	"time"
)

var threads = 1000

var RootCmd = cobra.Command{
	Run: func(cmd *cobra.Command, args []string) {
		var startTime = time.Now()
		var nowInUseChan = make(chan int, 100)
		task := func() {
			nowInUseChan <- 1
			defer func() {
				nowInUseChan <- -1
			}()
			time.Sleep(10)
		}

		// 实际执行任务
		for i := 0; i < threads; i++ {
			go task()
		}

		// 任务执行状态监控，在主协程中执行
		var nowInUse = 0
		var maxInUse = 0
		var finishedTask = 0
		for {
			if finishedTask < threads {
				select {
				case inUse := <-nowInUseChan:
					nowInUse += inUse
					if nowInUse > maxInUse {
						maxInUse = nowInUse
					}
					if inUse == -1 {
						// 如果监控到执行完毕所有的任务则退出循环
						finishedTask++
					}
				}
			} else {
				break
			}
		}
		duration := time.Since(startTime) // 从开始到当前所消耗的时间
		fmt.Printf("isVirtual: %s\tfinal: %.0fs|%d\tmaxInUse: %d\n", "Y", duration.Seconds(), duration.Nanoseconds()/1000, maxInUse)
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
