package com.agentgo.cli.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Limits and command policy for authenticated CLI / shell execution.
 *
 * @property timeoutMs wall-clock limit before a running process is destroyed
 * @property maxStdoutChars maximum captured standard output characters
 * @property maxStderrChars maximum captured standard error characters
 * @property poolSize number of CLI workers created at application startup
 * @property acquireTimeoutMs how long a request waits for an idle worker
 * @property allowedCommands when non-empty, only these command basenames may run
 * @property deniedCommands command basenames that are always rejected
 * @property denyShellOperators whether shell chaining / redirection / substitution is blocked
 */
@ConfigurationProperties(prefix = "agentgo.cli")
data class CliProperties(
    val timeoutMs: Long = 15_000,
    val maxStdoutChars: Int = 200_000,
    val maxStderrChars: Int = 200_000,
    val poolSize: Int = 10,
    val acquireTimeoutMs: Long = 5_000,
    val allowedCommands: List<String> = listOf("agentgo"),
    val deniedCommands: List<String> = DEFAULT_DENIED_COMMANDS,
    val denyShellOperators: Boolean = true,
) {
    companion object {
        /**
         * Built-in denylist covering destructive, privilege-escalation, nested-shell,
         * and remote-download helpers commonly abused through one-shot shell APIs.
         */
        val DEFAULT_DENIED_COMMANDS: List<String> = listOf(
            "rm", "rmdir", "del", "erase", "rd", "remove-item",
            "format", "mkfs", "mkfs.ext4", "mkfs.xfs", "diskpart", "fdisk", "parted",
            "dd", "shred", "cipher",
            "shutdown", "reboot", "poweroff", "halt", "init",
            "sudo", "su", "runas", "doas",
            "bash", "sh", "zsh", "fish", "powershell", "pwsh", "cmd", "cscript", "wscript",
            "python", "python3", "perl", "ruby", "node", "nodejs", "php", "lua",
            "chmod", "chown", "chgrp",
            "kill", "killall", "taskkill", "pskill",
            "reg", "regedit", "regedt32",
            "sc", "systemctl", "service", "launchctl",
            "iptables", "nft", "ufw", "firewall-cmd",
            "mount", "umount", "diskutil",
            "crontab", "at", "schtasks",
            "nc", "ncat", "netcat",
            "curl", "wget", "invoke-webrequest", "iwr", "iex",
            "certutil", "bitsadmin",
            "mshta", "rundll32",
        )
    }
}
