plugins {
    application
}

dependencies {
    implementation(project(":agentgo-core"))
}

application {
    mainClass.set("com.agentgo.app.MainKt")
}
