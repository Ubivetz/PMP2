package stu.cn.ua.pmp1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import stu.cn.ua.pmp1.ui.SettingsFragment

class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.menu_fragment, container, false)

        view.findViewById<Button>(R.id.btn_start_game).setOnClickListener {
            navigateTo(GameFragment())
        }

        view.findViewById<Button>(R.id.btn_settings).setOnClickListener {
            navigateTo(SettingsFragment())
        }

        return view
    }

    private fun navigateTo(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
