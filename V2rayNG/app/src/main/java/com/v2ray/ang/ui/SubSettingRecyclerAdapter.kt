package com.v2ray.ang.ui

import android.content.Intent
import android.graphics.Color
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.SubscriptionCache
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.helper.ItemTouchHelperAdapter
import com.v2ray.ang.helper.ItemTouchHelperViewHolder
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

class SubSettingRecyclerAdapter(val mActivity: SubSettingActivity) :
    RecyclerView.Adapter<SubSettingRecyclerAdapter.BaseViewHolder>(), ItemTouchHelperAdapter {

    private var list: MutableList<SubscriptionCache> = mutableListOf()

    init {
        updateList()
    }

    private fun updateList() {
        list = MmkvManager.decodeSubscriptions().toMutableList()
    }

    override fun getItemCount() = list.size

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(list, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        val subId = list[position].guid
        AlertDialog.Builder(mActivity).setMessage(R.string.del_config_comfirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                MmkvManager.removeSubscription(subId)
                updateList()
                notifyItemRemoved(position)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                notifyItemChanged(position)
            }
            .show()
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val cache = list[position]
        val subId = cache.guid
        val subItem = cache.subscription

        holder.tvName.text = subItem.remarks
        holder.tvUrl.text = subItem.url

        if (subItem.lastUpdated > 0) {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            holder.tvLastUpdated.text = mActivity.getString(
                R.string.title_update_time, 
                format.format(Date(subItem.lastUpdated))
            )
        } else {
            holder.tvLastUpdated.text = mActivity.getString(
                R.string.title_update_time, 
                mActivity.getString(R.string.title_not_updated)
            )
        }

        // پردازش و نمایش ترافیک و تاریخ انقضا
        if (subItem.total > 0L) {
            val usedFormat = Formatter.formatFileSize(mActivity, subItem.upload + subItem.download)
            val totalFormat = Formatter.formatFileSize(mActivity, subItem.total)
            
            val expireDate = if (subItem.expire > 0L) {
                val date = Date(subItem.expire * 1000L)
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            } else {
                "Unlimited"
            }
            
            holder.tvTrafficInfo.text = String.format(Locale.getDefault(), "Usage: %s / %s | Expire: %s", usedFormat, totalFormat, expireDate)
            holder.tvTrafficInfo.visibility = View.VISIBLE
        } else {
            holder.tvTrafficInfo.visibility = View.GONE
        }

        // پاک کردن لیسنر قبلی برای جلوگیری از باگ‌های RecyclerView
        holder.chkEnable.setOnCheckedChangeListener(null)
        holder.chkEnable.isChecked = subItem.enabled
        holder.chkEnable.setOnCheckedChangeListener { _, isChecked ->
            subItem.enabled = isChecked
            MmkvManager.encodeSubscription(subId, subItem)
        }

        holder.layoutEdit.setOnClickListener {
            val intent = Intent(mActivity, SubEditActivity::class.java)
            intent.putExtra("subId", subId)
            mActivity.startActivity(intent)
        }

        holder.layoutRemove.setOnClickListener {
            AlertDialog.Builder(mActivity).setMessage(R.string.del_config_comfirm)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    MmkvManager.removeSubscription(subId)
                    updateList()
                    notifyDataSetChanged()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        holder.layoutShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, subItem.url)
            mActivity.startActivity(
                Intent.createChooser(intent, mActivity.getString(R.string.title_configuration_share))
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycler_sub_setting, parent, false)
        return BaseViewHolder(view)
    }

    class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvUrl: TextView = itemView.findViewById(R.id.tv_url)
        val tvLastUpdated: TextView = itemView.findViewById(R.id.tv_last_updated)
        // تعریف المان متنی جدید برای ترافیک
        val tvTrafficInfo: TextView = itemView.findViewById(R.id.tv_traffic_info)
        
        val chkEnable: SwitchCompat = itemView.findViewById(R.id.chk_enable)
        val layoutEdit: View = itemView.findViewById(R.id.layout_edit)
        val layoutRemove: View = itemView.findViewById(R.id.layout_remove)
        val layoutShare: View = itemView.findViewById(R.id.layout_share)

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}
