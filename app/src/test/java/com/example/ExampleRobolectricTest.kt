package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.InitialData
import com.example.model.WorkerEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Karyasheel", appName)
    }

    @Test
    fun `worker id generator produces valid 8 digit id`() {
        val workerId = InitialData.generateWorkerId()
        assertEquals(8, workerId.length)
        assertTrue(workerId.all { it.isDigit() })
    }

    @Test
    fun `initial categories and sample workers are populated with production entities`() {
        assertTrue(InitialData.categories.isNotEmpty())
        assertTrue(InitialData.sampleWorkers.isNotEmpty())
        val firstWorker = InitialData.sampleWorkers[0]
        assertEquals("48271635", firstWorker.workerId)
        assertEquals("verified", firstWorker.verificationStatus)
        assertEquals("available", firstWorker.availability)
        assertEquals("cat_electrician", firstWorker.categoryId)
    }
}
