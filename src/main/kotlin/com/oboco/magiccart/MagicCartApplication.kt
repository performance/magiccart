package com.oboco.magiccart

import com.oboco.magiccart.config.MagicCartProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(MagicCartProperties::class)
class MagicCartApplication

fun main(args: Array<String>) {
	runApplication<MagicCartApplication>(*args)
}
