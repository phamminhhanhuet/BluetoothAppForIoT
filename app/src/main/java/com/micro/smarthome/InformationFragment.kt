package com.micro.smarthome

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_information.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InformationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InformationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var userData : DataSource? = DataSource.getData()
    private var navController : NavController? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        navController = container?.let { Navigation.findNavController(it) }
        return inflater.inflate(R.layout.fragment_information, container, false)
    }

    private var contactPhone : String? = ""
    private var contactEmail : String? = ""
    private var isContactPhone: Boolean? = true
    private var isContactEmail: Boolean? = true
    private var isContactMes : Boolean? = true


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        toolbarInfor.inflateMenu(R.menu.menu_connected)
        inflater.inflate(R.menu.menu_connected, menu)
    }

    override fun onStart() {
        super.onStart()

        val sharedPref = activity?.getSharedPreferences("com.micro.smarthome.PREFERENCE", Context.MODE_PRIVATE) ?: return
        val name = sharedPref.getString(USER_NAME, "Pham Hanh")
        val phone = sharedPref.getString(USER_PHONE_NUMBER, "01234567")
        val email = sharedPref.getString(USER_EMAIL, "youremail@gmail.com")
        val password = sharedPref.getString(USER_PASSWORD, "*******")
        val isPhone = sharedPref.getBoolean(USER_IS_PHONE, true)
        val isMes = sharedPref.getBoolean(USER_IS_MESSAGE, true)
        val isEmail = sharedPref.getBoolean(USER_IS_EMAIL, true)

        userData.run {
            /*editUsernameInformation.setText(userData?.fullname)
            editPhoneInformation.setText(userData?.phoneNumber)
            editEmailInformation.setText(userData?.email)
            editPasswordInformation.setText(userData?.password?.replace("^[-\\\\w.]+", "*"))
            checkPhoneInformation.isChecked = userData?.isConnectPhone.let { true }
            checkEmailInformation.isChecked = userData?.isConnectMail.let { true }
            checkMessageInformation.isChecked = userData?.isConnectMes.let { true }*/
            editUsernameInformation.setText(name)
            editPhoneInformation.setText(phone)
            editEmailInformation.setText(email)
            editPasswordInformation.setText(password?.replace("^[-\\\\w.]+", "*"))
            checkPhoneInformation.isChecked = isPhone
            checkMessageInformation.isChecked = isMes
            checkEmailInformation.isChecked = isEmail
            confirmInformation.setOnClickListener{
                userData?.fullname = editUsernameInformation.text?.toString()
                userData?.phoneNumber = editPhoneInformation.text?.toString()
                userData?.email = editEmailInformation.text?.toString()
                userData?.password = editPasswordInformation.text?.toString()
                userData?.isConnectPhone = checkPhoneInformation.isChecked
                userData?.isConnectMail = checkEmailInformation.isChecked
                userData?.isConnectMes = checkMessageInformation.isChecked

                contactPhone = userData?.phoneNumber
                contactEmail = userData?.email
                isContactPhone = userData?.isConnectPhone
                isContactEmail = userData?.isConnectMail
                isContactMes = userData?.isConnectMes

                val sharedPref = activity?.getSharedPreferences("com.micro.smarthome.PREFERENCE", Context.MODE_PRIVATE) ?: return@setOnClickListener
                with (sharedPref.edit()) {
                    putString(USER_NAME, userData?.fullname)
                    putString(USER_IS_PHONE, userData?.phoneNumber)
                    putString(USER_EMAIL, userData?.email)
                    putString(USER_PASSWORD, userData?.password)
                    putBoolean(USER_IS_MESSAGE, userData?.isConnectMes!!)
                    putBoolean(USER_IS_EMAIL, userData?.isConnectMail!!)
                    putBoolean(USER_IS_PHONE, userData?.isConnectPhone!!)
                    apply()
                }

                navController?.navigate(R.id.action_informationFragment_to_ControlFragment)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InformationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InformationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}