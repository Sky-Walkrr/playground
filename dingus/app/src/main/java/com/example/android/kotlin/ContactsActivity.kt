package com.example.android.kotlin

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.*
import android.widget.EditText
import android.widget.TextView
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_contacts.*
import kotlinx.android.synthetic.main.input_contact_dialog.view.*
import java.io.IOException

/**
 * Created by gchfeng on 2018/1/26.
 * E-mail:gchfeng.me@gmail.com
 */
class ContactsActivity : AppCompatActivity(), TextWatcher {

    private val mContacts: ArrayList<Contact> by lazy { loadContacts() }
    private val mAdapter: ContactsAdapter by lazy { ContactsAdapter(mContacts) }
    private val mPrefs: SharedPreferences by lazy { getPreferences(Context.MODE_PRIVATE) }
    private val coordinatorLayout by lazy { findViewById<CoordinatorLayout>(R.id.coordinator) }
    private lateinit var mFirstNameEdit: EditText
    private lateinit var mLastNameEdit: EditText
    private lateinit var mEmailEdit: EditText
    private var mEntryValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        setSupportActionBar(toolbar)
        fab.setOnClickListener({ showAddDialog(-1) })
        setupRV()
    }

    override fun afterTextChanged(s: Editable?) {
        val isNotEmpty: (EditText) -> Boolean = {
            !it.text.isNullOrEmpty()
        }

        val isMatchEmail: (EditText) -> Boolean = {
            Patterns.EMAIL_ADDRESS.matcher(it.text).matches()
        }
        //or
//        val isEmailValid = mEmailEdit.text.matches(Regex(Patterns.EMAIL_ADDRESS.pattern()))
//        val validDrawable = ContextCompat.getDrawable(this, R.drawable.ic_pass)
//        val invalidDrawable = ContextCompat.getDrawable(this, R.drawable.ic_fail)
//        mFirstNameEdit.setCompoundDrawablesWithIntrinsicBounds(null, null,
//                if (isNotEmpty(mFirstNameEdit)) validDrawable else invalidDrawable, null)
//        mLastNameEdit.setCompoundDrawablesWithIntrinsicBounds(null, null,
//                if (isNotEmpty(mLastNameEdit)) validDrawable else invalidDrawable, null)
//        mEmailEdit.setCompoundDrawablesWithIntrinsicBounds(null, null,
//                if (isMatchEmail(mEmailEdit)) validDrawable else invalidDrawable, null)
//        mEntryValid = isNotEmpty(mFirstNameEdit) and isNotEmpty(mLastNameEdit) and isMatchEmail(mEmailEdit)

        //Extension～～∠( ᐛ 」∠)＿
        mEntryValid = mFirstNameEdit.validate { isNotEmpty(this) } and
                mLastNameEdit.validate { isNotEmpty(this) } and
                mEmailEdit.validate { isMatchEmail(this) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_contacts, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_clear -> {
                clearContacts()
                return true
            }
            R.id.action_generate -> {
                genContacts()
                return true
            }
            R.id.action_sort_by_first_name -> {
                mContacts.sortBy {
                    it.firstName
                }
                saveContact()
                mAdapter.notifyDataSetChanged()
                return true
            }
            R.id.action_sort_by_last_name -> {
                mContacts.sortBy {
                    it.lastName
                }
                saveContact()
                mAdapter.notifyDataSetChanged()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    private fun clearContacts() {
        mContacts.clear()
        saveContact()
        mAdapter.notifyDataSetChanged()
    }

    private fun genContacts() {
        val list = GsonUtilLazy.instance.fromJson<ArrayList<Contact>>(readContactJsonFile(),
                (object : TypeToken<ArrayList<Contact>>() {}).type)
        mContacts.addAll(list)
        saveContact()
        mAdapter.notifyDataSetChanged()
    }

    private fun readContactJsonFile(): String? {
        var string = null
        val buffer: ByteArray
        try {
            val inputStream = assets.open("mock_contacts.json")
            val size = inputStream.available()
            buffer = ByteArray(size)
            inputStream.read(buffer)
            Util.close(inputStream)
            return String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return string
    }

    @SuppressWarnings("InflateParams")
    fun showAddDialog(contactPos: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.input_contact_dialog, null)
        mFirstNameEdit = dialogView.edittext_firstname
        mLastNameEdit = dialogView.edittext_lastname
        mEmailEdit = dialogView.edittext_email

        mFirstNameEdit.addTextChangedListener(this)
        mLastNameEdit.addTextChangedListener(this)
        mEmailEdit.addTextChangedListener(this)

        val isEditing = contactPos >= 0
        val dialogTitle = if (isEditing) getString(R.string.edit_contact) else getString(R.string.new_contact)
        val builder = AlertDialog.Builder(this).setView(dialogView)
                .setTitle(dialogTitle)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
        val dialog = builder.show()
        if (isEditing) {
            val contact = mContacts[contactPos]
            mFirstNameEdit.setText(contact.firstName)
            mFirstNameEdit.isEnabled = contact.firstName.isNullOrEmpty()
            mLastNameEdit.setText(contact.lastName)
            mLastNameEdit.isEnabled = contact.lastName.isNullOrEmpty()
            mEmailEdit.setText(contact.email)
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
            if (mEntryValid) {
                if (isEditing) {
                    val editContact = mContacts[contactPos]
                    editContact.email = mEmailEdit.text.toString()
                    mContacts[contactPos] = editContact
                    mAdapter.notifyItemChanged(contactPos)
                } else {
                    val newContact = Contact(mFirstNameEdit.text.toString(), mLastNameEdit.text.toString(),
                            mEmailEdit.text.toString())
                    mContacts.add(newContact)
                    mAdapter.notifyItemInserted(mContacts.size)
                }
                saveContact()
                dialog.dismiss()
            } else {
                Util.showToast(this, getString(R.string.contact_not_valid))
            }
        })

    }

    private fun setupRV() {
        val recyclerView = findViewById<RecyclerView>(R.id.contact_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        //use ItemTouchHelper to implement swipe to delete
        val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                val position = viewHolder?.adapterPosition
                if (position == null) return
                val contact = mContacts[position]
                mContacts.removeAt(position)
                mAdapter.notifyItemRemoved(position)
                saveContact()
                Util.showSnackBar(coordinatorLayout, "You deleted one contact ∠( ᐛ 」∠)＿",
                        Snackbar.LENGTH_INDEFINITE, "Undo", {
                    mContacts.add(position, contact)
                    mAdapter.notifyItemInserted(position)
                })
            }
        })
        helper.attachToRecyclerView(recyclerView)
    }

    private fun loadContacts(): ArrayList<Contact> {
        val set = mPrefs.getStringSet(CONTACT_KEY, HashSet<String>())

        //mapTo
        val list = ArrayList<Contact>()
        return set.mapTo(list, {
            GsonUtilLazy.instance.fromJson(it, Contact::class.java)
        })
//        set.map {
//            list.add(GsonUtilLazy.instance.fromJson(it, Contact::class.java))
//        }
//        return list
    }

    private fun saveContact() {
        val editor = mPrefs.edit()
        val set = HashSet<String>()
        //replace loop with stdlib operation
//        for (c in mContacts) {
//            set.add(GsonUtilHungry.getInstance().toJson(c))
//        }

        mContacts.mapTo(set, {
            GsonUtilLazy.instance.toJson(it)
        })
//        mContacts.map {
//            set.add(GsonUtilLazy.instance.toJson(it))
//        }
        editor.putStringSet(CONTACT_KEY, set)
        editor.apply()
    }

    companion object {
        private val CONTACT_KEY = "contact_key"
        private val TAG = ContactsActivity::class.java.getSimpleName()
    }

    private inner class ContactsAdapter(private val list: ArrayList<Contact>?) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            //https://stackoverflow.com/questions/26596436/error-using-the-recyclerview-the-specified-child-already-has-a-parent
            return ViewHolder(LayoutInflater.from(this@ContactsActivity).inflate(R.layout.contact_list_item, parent, false))
        }

        override fun getItemCount(): Int {
            return list?.size ?: 0
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val data = list?.get(position)
            holder?.nameLabel?.text = data?.firstName + "  " + data?.lastName
            holder?.emailLabel?.text = data?.email
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameLabel by lazy { itemView.findViewById<TextView>(R.id.textview_name) }
            val emailLabel by lazy { itemView.findViewById<TextView>(R.id.textview_email) }

            init {
                itemView.setOnClickListener({
                    showAddDialog(adapterPosition)
                })
            }
        }

    }
}