package com.example.todo.fragments


import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.databinding.DialogAddTaskBinding
import com.example.todo.databinding.FragmentHomeBinding
import com.example.todo.utils.ToDoAdapter
import com.example.todo.utils.ToDoData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class HomeFragment : Fragment(), ToDoAdapter.ToDoAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navController: NavController
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: ToDoAdapter
    private lateinit var mList:MutableList<ToDoData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        init(view)
        getDataFromtheFirebase()
        binding.fab.setOnClickListener{
            showDialog()
        }
        binding.logout.setOnClickListener {
            logout()
        }
        binding.profile.setOnClickListener {
            Toast.makeText(context, "This feature is not yet implemented (Your profile will appear here)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        val builder: AlertDialog.Builder? = context?.let { AlertDialog.Builder(it) }
        builder?.setMessage("Are u want to logout ?")?.setTitle("Logout")
            ?.setPositiveButton("Logout") { dialog, which ->
                Firebase.auth.signOut()
                navController.navigate(R.id.action_homeFragment_to_signInFragment)
            }?.setNegativeButton("Cancel") { dialog, which ->
                // Do something else.
        }

        val dialog: AlertDialog? = builder?.create()
        dialog?.show()
    }

    private fun getDataFromtheFirebase() {
        binding.progressBar.visibility=View.VISIBLE
        databaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for(taskSnapshot in snapshot.children){
                    val todoTask=taskSnapshot.key?.let{
                        ToDoData(it,taskSnapshot.value.toString())
                    }
                    if(todoTask!=null){
                        mList.add(todoTask)
                    }
                }
                binding.progressBar.visibility=View.GONE
                adapter.notifyDataSetChanged()
                if(mList.size == 0) {
                    binding.blankBg.visibility= View.VISIBLE
                }else{
                    binding.blankBg.visibility= View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun init(view: View) {
        navController=Navigation.findNavController(view)
        auth= FirebaseAuth.getInstance()
        databaseRef=FirebaseDatabase.getInstance().reference
            .child("Tasks").child(auth.currentUser?.uid.toString())

        binding.rev.setHasFixedSize(true)
        binding.rev.layoutManager=LinearLayoutManager(context)
        mList= mutableListOf()
        adapter= ToDoAdapter(mList)
        adapter.setListener(this)
        binding.rev.adapter=adapter
    }

    private fun showDialog(){
        val dialog= context?.let { Dialog(it) }

       dialog?.setCancelable(true)
        dialog?.show()

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val bdg = DialogAddTaskBinding.inflate(layoutInflater)
        dialog?.setContentView(bdg.root)
        bdg.cancel.setOnClickListener {
            dialog?.dismiss()
        }
        bdg.join.setOnClickListener {
            val task = bdg.newTask.text.toString()
            if (task.isNotEmpty()) {
                uploadTask(task)
            } else {
                Toast.makeText(context, "Please Enter new task", Toast.LENGTH_SHORT).show()
            }
            bdg.newTask.text = null
            dialog?.dismiss()
        }
    }

    private fun uploadTask(task: String) {
        binding.progressBar.visibility=View.VISIBLE
        databaseRef.push().setValue(task).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(context, "Task added Successfully !!", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, it.exception?.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        binding.progressBar.visibility=View.GONE
    }

    override fun onDeleteTaskBtnClicked(toDoData: ToDoData) {
        databaseRef.child(toDoData.taskId).removeValue().addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(context, "Task Deleted Successfully", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, it.exception?.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditTaskBtnClicked(toDoData: ToDoData) {
        editShowDialog(toDoData.task, toDoData.taskId)
    }

    private fun editShowDialog(task: String, taskId: String) {
        val dialog= context?.let { Dialog(it) }
        val bdg = DialogAddTaskBinding.inflate(layoutInflater)
        dialog?.setContentView(bdg.root)
        dialog?.setCancelable(true)

        bdg.dtitle.setText(R.string.edit_your_task)
        bdg.newTask.setText(task)
        dialog?.show()

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )


        bdg.cancel.setOnClickListener {
            dialog?.dismiss()
        }
        bdg.join.setOnClickListener {
            val nTask= bdg.newTask.text.toString()
            if (nTask.isNotEmpty()) {
                updateTask(nTask,taskId)
            } else {
                Toast.makeText(context, "Please Enter some task", Toast.LENGTH_SHORT).show()
            }
            bdg.newTask.text = null
            dialog?.dismiss()
        }
    }

    private fun updateTask(nTask: String, taskId: String) {
        binding.progressBar.visibility=View.VISIBLE
        val map = HashMap<String,Any>()
        map[taskId]=nTask
        databaseRef.updateChildren(map).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(context, "Task Updated Successfully", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, it.exception?.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        binding.progressBar.visibility=View.GONE
    }
}