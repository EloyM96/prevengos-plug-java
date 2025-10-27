package com.prevengos.plug.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HubApiApplication

fun main(args: Array<String>) {
    runApplication<HubApiApplication>(*args)
}
