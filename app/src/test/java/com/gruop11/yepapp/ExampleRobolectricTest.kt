package com.gruop11.yepapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gruop11.yepapp.yep.data.SeedData
import com.gruop11.yepapp.yep.model.CapstoneSubmission
import com.gruop11.yepapp.yep.model.SyncStatus
import com.gruop11.yepapp.yep.sync.YepSyncMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("YEP Platform", appName)
  }

  @Test
  fun `sync mapper round trip preserves web schema and local pending queue`() {
    val seed = SeedData.createInitialSnapshot()
    val remoteJson = YepSyncMapper.toJson(seed)
    assertTrue(remoteJson.has("users"))
    assertTrue(remoteJson.has("exchanges"))
    assertTrue(remoteJson.has("enrollments"))
    assertTrue(remoteJson.has("submissions"))
    assertTrue(remoteJson.has("opportunities"))
    assertTrue(remoteJson.has("expressionsOfInterest"))
    assertTrue(remoteJson.has("auditLogs"))
    assertTrue(remoteJson.has("notifications"))

    val pendingLocal = seed.copy(
      submissions = seed.submissions + CapstoneSubmission(
        id = "sub_local_pending",
        userId = "u_amara",
        exchangeId = "ex_cloud",
        projectTitle = "Offline Queued Project",
        architectureDescription = "Buffered while offline",
        githubUrl = "https://github.com/example/offline",
        demoUrl = "https://demo.example.com",
        keyTakeaways = "Resilient queue",
        syncStatus = SyncStatus.PENDING_SYNC,
        submittedAt = 1731585600000L,
      )
    )
    val merged = YepSyncMapper.mergeFromRemoteJson(remoteJson, pendingLocal)
    assertTrue(merged.submissions.any { it.id == "sub_local_pending" && it.syncStatus == SyncStatus.PENDING_SYNC })
    assertEquals(seed.users.size, merged.users.size)
  }
}
