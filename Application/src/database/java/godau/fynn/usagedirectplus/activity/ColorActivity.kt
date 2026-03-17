package godau.fynn.usagedirectplus.activity

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import godau.fynn.usagedirectplus.databinding.ActivityColorBinding
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import godau.fynn.usagedirectplus.persistence.combined.TimeAppColor
import godau.fynn.usagedirectplus.thread.icon.IconThread
import godau.fynn.usagedirectplus.view.adapter.ColorAdapter
import godau.fynn.usagedirectplus.view.adapter.ColorAdapterTouchCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Arrays

class ColorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityColorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityColorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch(Dispatchers.IO) {
            val database = HistoryDatabase.get(this@ColorActivity)

            val timeAppColors = database.getAppColorDao().getTimeAppColors()
            database.close()

            withContext(Dispatchers.Main) {
                binding.recyclerview.layoutManager = LinearLayoutManager(this@ColorActivity)

                val touchHelper = ItemTouchHelper(ColorAdapterTouchCallback())
                touchHelper.attachToRecyclerView(binding.recyclerview)

                binding.recyclerview.adapter = ColorAdapter(timeAppColors, touchHelper)

                IconThread(
                    Arrays.stream(timeAppColors)
                        .map(TimeAppColor::applicationId)
                        .toArray { size -> arrayOfNulls<String>(size) },
                    binding.recyclerview.layoutManager!!, this@ColorActivity
                ).start()

                // Refresh calling activity after database call
                setResult(Activity.RESULT_OK)
            }
        }
    }

    override fun onDestroy() {
        if (::binding.isInitialized) {
            binding.recyclerview.adapter = null
        }

        super.onDestroy()
    }
}
