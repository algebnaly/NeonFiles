package  com.algebnaly.neonfiles.core.model
import com.algebnaly.neonfiles.filesystem.StorageConfig

data class StorageLocation(
    val id: Int = 0,
    val name: String,
    val path: String,
    val config: StorageConfig,
)