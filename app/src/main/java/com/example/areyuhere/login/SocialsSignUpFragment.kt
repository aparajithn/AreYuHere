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
    var signInFailed = false
    private var alreadyExists = false
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
        emailButton = view.findViewById(R.id.email_button)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context!!, gso)

        googleSignIn.setOnClickListener {
            signIn()
        }

        /*
        * purpose: adds teacher UIDs to a mutable list
        * inputs: snapshot of db from the teacherlist node
        * outputs: none
        * TODO:
        */
        viewModel.teacherListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    uidListT.add(snapshot.key.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        /*
        * purpose: adds student UIDs to a mutable list
        * inputs: snapshot of db from the studentlist node
        * outputs: none
        * TODO:
        */
        viewModel.studentListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    uidListS.add(snapshot.key.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        //go to email sign up page if selected
        emailButton.setOnClickListener{
            Navigation.createNavigateOnClickListener(R.id.action_socialsSignUpFragment_to_signUpFragment, null)
            view.findNavController().navigate(R.id.action_socialsSignUpFragment_to_signUpFragment)
        }

       return view
    }

    /*
    * purpose: attempts to sign in with Google account
    * inputs: snapshot of db from the teacherlist node
    * outputs: none
    * TODO: add navigation to appropriate home page on signup as well (currently only navigates when account already
    *  exists, can remove the navigation part from there but leave the error messages and just handle navigation here)
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode.equals(RC_SIGN_IN)) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, let user know
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    /*
    * purpose: creates user in Firebase Auth based on Google credentials
    * inputs: google credentials
    * outputs: new user created in auth via Google credentials and/or they are navigated to a homepage, or error messages display
    * TODO: if signin fails or they are signed in but account already exists, they are stuck on the current page, with
    *  only clicking back to get where they need to be. We should auto-navigate for them to the appropriate page
    */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "signInWithCredential:success")

                    val user = auth.currentUser
                    viewModel.currentEmail = user?.email.toString()

                    //iterators to go through the lists of UIDs of all users
                    val uidIteratorT = uidListT.iterator()
                    val uidIteratorS = uidListS.iterator()

                    //checking if user already has a teacher account associated with this google account
                    while(uidIteratorT.hasNext())
                    {
                        if (user?.uid.toString().equals(uidIteratorT.next()))
                        {
                            alreadyExists = true

                            //if they are trying to sign up as a teacher, but already have a teacher account linked to this Google account
                            //then just sign them in to that teacher account. But if they are trying to sign up as a student with that same
                            // Google account, then we let them know that account already exists
                            if (teacher_slider_socials.isChecked)
                            {
                                Navigation.createNavigateOnClickListener(R.id.action_socialsSignUpFragment_to_teacherHomeFragment, null)
                                view?.findNavController()?.navigate(R.id.action_socialsSignUpFragment_to_teacherHomeFragment)
                            }
                            else
                            {
                                Toast.makeText(context, "Account already exists!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    //checking if user already has a student account associated with this google account
                    while(uidIteratorS.hasNext())
                    {
                        if (user?.uid.toString().equals( uidIteratorS.next()))
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

                    //alreadyExists would be true if this Google account has a student or teacher account already tied to it
                    //so since it is false, we can safely sign this user up with their google account
                    if (!alreadyExists)
                    {
                        signupDb(user!!.email.toString(), user!!.displayName.toString(), user.uid)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure")
                    signInFailed = true
                }
            }
    }

    /*
    * purpose: creates user in Firebase realtime db based on Google credentials
    * inputs: email, name, and index extracted from the auth info taken from google sign in
    * outputs: new user is added to either studentlist or teacherlist
    * TODO:
    */
    fun signupDb(email: String, name: String, index: String){
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
            viewModel.studentListRef.child(index).updateChildren(userData)
            Navigation.createNavigateOnClickListener(R.id.action_socialsSignUpFragment_to_studentHomeFragment, null)
            view?.findNavController()?.navigate(R.id.action_socialsSignUpFragment_to_studentHomeFragment)
        }
    }

    /*
    * purpose: starts intent to sign in with google
    * inputs: none
    * outputs: none
    * TODO:
    */
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    //some constants that need to be here for the google signin to work
    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}