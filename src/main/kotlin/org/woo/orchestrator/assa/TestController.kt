package org.woo.orchestrator.assa

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(
    val testService: TestService,
) {
    @PostMapping("/test")
    suspend fun init() {
        testService.init()
    }
}
