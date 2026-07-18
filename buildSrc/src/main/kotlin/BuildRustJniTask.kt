import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class BuildRustJniTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val jniCrateSrc: DirectoryProperty

    // list of supported abi, e.g. x86_64, arm64-v8a
    @get:Input
    abstract val abiList: ListProperty<String>

    @get:Input
    abstract val ndkHome: Property<String>

    @get:Input
    abstract val rustFlags: Property<String>

    @get:Input
    @get:Optional
    abstract val cargoFeatures: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun build() {
        val out = outputDirectory.get().asFile
        out.mkdirs()

        execOperations.exec {
            workingDir = jniCrateSrc.get().asFile.parentFile // nfscrs_jni 根目录
            environment("ANDROID_NDK_HOME", ndkHome.get())
            environment("RUSTFLAGS", rustFlags.get())
            
            commandLine(
                buildList {
                    add("cargo")
                    add("ndk")
                    
                    abiList.get().forEach {
                        add("-t"); add(it)
                    }

                    if (cargoFeatures.isPresent && cargoFeatures.get().isNotEmpty()) {
                        add("--features")
                        add(cargoFeatures.get().joinToString(","))
                    }
                    
                    add("-o"); add(out.absolutePath)
                    add("build"); add("--release")
                }
            )
        }
    }
}
