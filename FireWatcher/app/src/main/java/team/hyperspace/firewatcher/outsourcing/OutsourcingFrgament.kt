package team.hyperspace.firewatcher.outsourcing

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.google.common.collect.Lists
import team.hyperspace.firewatcher.R
import java.util.*

class OutsourcingFrgament : Fragment() {

    private lateinit var image : ImageView
    private lateinit var bigTree : ImageView
    private lateinit var panel : View
    private lateinit var noImage : View
    private val images : Queue<Int> = ArrayDeque(Lists.newArrayList(R.drawable.urban_fires, R.drawable.wildfires, R.drawable.residential_area))
    private val bigTrees : Queue<Int> = ArrayDeque(Lists.newArrayList(R.drawable.big_tree_after, R.drawable.big_tree_after_2, R.drawable.big_tree_after_3))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_outsourcing, container, false)
        bigTree = view.findViewById(R.id.big_tree)
        image = view.findViewById(R.id.outsourcing_image)
        if (images.size > 0) {
            val drawable : Int = images.poll()
            image.setImageResource(drawable)
        }
        panel = view.findViewById(R.id.outsourcing_panel)
        noImage = view.findViewById(R.id.no_image)
        init(view!!)
        return view
    }

    private fun init(view : View) {
        val onRadioBtnClickListener : OnClickListener = object : View.OnClickListener {
            override fun onClick(view: View?) {
                Toast.makeText(context, "Thanks for your effort", Toast.LENGTH_SHORT).show()
                if (images.size > 0) {
                    val drawable : Int = images.poll()
                    image.setImageResource(drawable)
                } else {
                    panel.visibility = View.INVISIBLE
                    noImage.visibility = View.VISIBLE
                }

                if (bigTrees.size > 0) {
                    val drawable : Int = bigTrees.poll()
                    bigTree.setImageResource(drawable)
                }
            }
        }
        view.findViewById<View>(R.id.forest).setOnClickListener(onRadioBtnClickListener)
        view.findViewById<View>(R.id.residentail_area).setOnClickListener(onRadioBtnClickListener)
        view.findViewById<View>(R.id.urban_fires).setOnClickListener(onRadioBtnClickListener)
        view.findViewById<View>(R.id.wild_fires).setOnClickListener(onRadioBtnClickListener)
        view.findViewById<View>(R.id.others).setOnClickListener(onRadioBtnClickListener)
        view.findViewById<View>(R.id.fire_extinguisher).setOnClickListener(onRadioBtnClickListener)
        view.findViewById<View>(R.id.hydrant).setOnClickListener(onRadioBtnClickListener)
        view.findViewById<View>(R.id.narrow).setOnClickListener(onRadioBtnClickListener)
    }
}