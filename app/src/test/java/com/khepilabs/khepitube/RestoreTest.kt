package com.khepilabs.khepitube

import org.junit.Test
import java.io.File

class RestoreTest {
    @Test
    fun doFullReset() {
        val root = File("C:/Users/M Mohan/Downloads/KhepiTube-source-final")

        val p1 = ProcessBuilder("git", "reset", "--hard", "HEAD")
            .directory(root)
            .redirectErrorStream(true)
            .start()
        val o1 = p1.inputStream.bufferedReader().readText()
        p1.waitFor()
        println("GIT RESET: $o1")

        val p2 = ProcessBuilder("git", "clean", "-fd")
            .directory(root)
            .redirectErrorStream(true)
            .start()
        val o2 = p2.inputStream.bufferedReader().readText()
        p2.waitFor()
        println("GIT CLEAN: $o2")
    }
}
