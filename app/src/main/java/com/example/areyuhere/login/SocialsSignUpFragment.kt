package com.example.areyuhere.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.areyuhere.R
import com.example.areyuhere.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_signup_socials.*


class SocialsSignUpFragment: Fragment() {
    private lateinit var googleSignIn: SignInButton
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var emailButton: Button
    var SignInFailed = false
    var alreadyExists = false
    val uidListT = mutableListOf<String>()
    val uidListS = mutableListOf<String>()

    val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup_socials, container, false)

        googleSignIn = view.findViewById(R.id.google_signin)
        auth = FirebaseAuth.getInstance()
        emailButton = view.findViewById(R.id.email_button)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context!!, gso)

        googleSignIn.setOnClickListener {
            signIn()
        }
        viewModel.teacherListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    uidListT.add(snapshot.key.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        viewModel.getStatus.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    uidListS.add(snapshot.key.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        emailButton.setOnClickListener{
            Navigation.createNavigateOnClickListener(R.id.action_socialsSignUpFragment_to_signUpFragment, null)
            view.findNavController().navigate(R.id.action_socialsSignUpFragment_to_signUpFragment)
        }

       return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        // [START_EXCLUDE silent]
        // [END_EXCLUDE]
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    viewModel.currentEmail = user?.email.toString()

//check if this account is already a user of any kind
                    val uidIteratorT = uidListT.iterator()
                    val uidIteratorS = uidListS.iterator()
//checking if user already has a teacher account associated with this google account
                    while(uidIteratorT.hasNext())
                    {
                        if (user?.uid.toString().equals(uidIteratorT.next()))
                        {
                            alreadyExists = true
                            if (teacher_slider_socials.isChecked)
                            {
                                Navigation.createNavigateOnClickListener(R.id.action_socialsSignUpFragment_to_teacherHomeFragment, null)
                                view?.findNavController()?.navigate(R.id.action_socialsSignUpFragment_to_teacherHomeFragment)
                            }
                            else
                            {
                                Toast.makeText(context, "Account already  exists!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
//checking if user already has a student account associated with this google account
                    while(uidIteratorS.hasNext())
                    {
                        if (user?.uid.toString().equals(uidIteratorS.next()))
                        {
                            alreadyExists = true
                            if (teacher_slider_socials.isChecked)
                            {
                                Toast.makeText(context, "Account already  exists!", Toast.LENGTH_SHORT).show()
                            }
                            else
                            {
                                Navigation.createNavigateOnClickListener(R.id.action_socialsSignUpFragment_to_studentHomeFragment, null)
                                view?.findNavController()?.navigate(R.id.action_socialsSignUpFragment_to_studentHomeFragment)
                            }
                        }
                    }
                    if (!alreadyExists)
                    {
                        signup_db(user!!.email.toString(), user!!.displayName.toString(), user.uid)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure")
                    SignInFailed = true
                }

            }
    }

    fun signup_db(email: String, name: String, index: String){
        val userData: MutableMap<String, Any> = HashMap()

        userData["email"] = email
        userData["name"] = name
        userData["preferred display name"] = name

        if (teacher_slider_socials.isChecked)
        {
            viewModel.teacherListRef.child(index).updateChildren(userData)
            Navigation.createNavigateOnClickListener(R.id.action_socialsSignUpFragment_to_teacherHomeFragment, null)
            view?.findNavController()?.navigate(R.id.action_socialsSignUpFragment_to_teacherHomeFragment)
        }
        else
        {
            viewModel.getStatus.child(index).updateChildren(userData)
            Navigation.createNavigateOnClickListener(R.id.action_socialsSignUpFragment_to_studentHomeFragment, null)
            view?.findNavController()?.navigate(R.id.action_socialsSignUpFragment_to_studentHomeFragment)
        }

    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent,
            RC_SIGN_IN
        )
    }
    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}