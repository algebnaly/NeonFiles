package com.algebnaly.neonfiles.ui.utils

import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import okio.FileSystem
import okio.buffer
import okio.source
import java.nio.file.Files
import java.nio.file.Path

class NioPathFetcher(
    private val path: Path
): Fetcher {
    override suspend fun fetch(): FetchResult {
        val inputStream = Files.newInputStream(path)
        val bufferedSource = inputStream.source().buffer()
        val okioFileSystem: FileSystem = FileSystem.SYSTEM

        val imageSource = ImageSource(bufferedSource, fileSystem = okioFileSystem)
        val sourceResult = SourceFetchResult(
            imageSource,
            mimeType = null,
            dataSource = DataSource.NETWORK
        )
        return sourceResult
    }
    class Factory: Fetcher.Factory<Path>{
        override fun create(data: Path, options: Options, imageLoader: ImageLoader): Fetcher {
            return NioPathFetcher(data)
        }
    }
}