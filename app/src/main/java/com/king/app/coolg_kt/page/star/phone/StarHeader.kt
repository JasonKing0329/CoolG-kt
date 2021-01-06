package com.king.app.coolg_kt.page.star.phone

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.ViewModelFactory
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterStarPhoneHeaderBinding
import com.king.app.coolg_kt.page.pub.BaseTagAdapter
import com.king.app.coolg_kt.page.pub.BaseTagAdapter.OnItemSelectListener
import com.king.app.coolg_kt.page.pub.TagAdapter
import com.king.app.coolg_kt.page.star.*
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.utils.StarRatingUtil
import com.king.app.coolg_kt.view.widget.StarRatingView
import com.king.app.coolg_kt.view.widget.StarRatingView.OnStarChangeListener
import com.king.app.gdb.data.entity.FavorStarOrder
import com.king.app.gdb.data.entity.Star
import com.king.app.gdb.data.entity.StarRating
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.relation.StarRelationship
import com.king.app.gdb.data.relation.StarStudioTag

/**
 * Created by Administrator on 2018/8/12 0012.
 */
class StarHeader : OnStarChangeListener {
    private var mRatingModel: StarRatingViewModel? = null
    private var mModel: StarViewModel? = null
    private var mBinding: AdapterStarPhoneHeaderBinding? = null
    var onHeadActionListener: OnHeadActionListener? = null
    private var selectedTag: StarStudioTag? = null
    private var relationshipAdapter = StarRelationshipAdapter()
    private var ordersAdapter = StarOrdersAdapter()
    private var tagAdapter = TagAdapter()

    private fun <AVM: AndroidViewModel> getActivityViewModel(context: Context, vm: Class<AVM>): AVM = ViewModelProvider((context as FragmentActivity).viewModelStore, ViewModelFactory(CoolApplication.instance)).get(vm)

    fun bind(
        binding: AdapterStarPhoneHeaderBinding,
        star: Star,
        relationships: List<StarRelationship>,
        studioList: List<StarStudioTag>,
        tagList: List<Tag>
    ) {
        mBinding = binding
        binding.starFace.setOnStarChangeListener(this)
        binding.starBody.setOnStarChangeListener(this)
        binding.starDk.setOnStarChangeListener(this)
        binding.starPassion.setOnStarChangeListener(this)
        binding.starVideo.setOnStarChangeListener(this)
        binding.starSex.setOnStarChangeListener(this)
        binding.starPrefer.setOnStarChangeListener(this)
        bindBasicInfo(binding, star)
        bindRelationships(binding, relationships)
        bindRatings(binding, star)
        bindOrders(binding, star)
        bindStudios(binding, studioList)
        bindTags(binding, tagList)
    }

    private fun bindTags(binding: AdapterStarPhoneHeaderBinding, tagList: List<Tag>) {
        if (tagList.isEmpty()) {
            binding.tvTagsTitle.visibility = View.VISIBLE
            binding.ivTagDelete.visibility = View.GONE
            binding.rvTags.visibility = View.GONE
        } else {
            binding.tvTagsTitle.visibility = View.GONE
            binding.ivTagDelete.visibility = View.VISIBLE
            binding.rvTags.visibility = View.VISIBLE
            binding.ivTagDelete.setOnClickListener {
                tagAdapter.toggleDelete()
                tagAdapter.notifyDataSetChanged()
            }
            if (binding.rvTags.adapter == null) {
                binding.rvTags.layoutManager = LinearLayoutManager(binding.rvTags.context, LinearLayoutManager.HORIZONTAL, false)
                binding.rvTags.addItemDecoration(object : ItemDecoration() {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        val position = parent.getChildLayoutPosition(view)
                        if (position > 0) {
                            outRect.left = ScreenUtils.dp2px(10f)
                        }
                    }
                })
                tagAdapter.onDeleteListener = object : TagAdapter.OnDeleteListener {
                    override fun onDelete(position: Int, bean: Tag) {
                        onHeadActionListener?.onDeleteTag(bean)
                    }
                }
                tagAdapter.setOnItemLongClickListener(object : BaseBindingAdapter.OnItemLongClickListener<Tag> {
                    override fun onLongClickItem(view: View, position: Int, data: Tag) {
                        tagAdapter.toggleDelete()
                        tagAdapter.notifyDataSetChanged()
                    }
                })
                tagAdapter.list = tagList
                binding.rvTags.adapter = tagAdapter
            } else {
                tagAdapter.list = tagList
                tagAdapter.notifyDataSetChanged()
            }
        }
        binding.ivTagAdd.setOnClickListener { onHeadActionListener?.onAddTag() }
    }

    private fun bindStudios(binding: AdapterStarPhoneHeaderBinding, studioList: List<StarStudioTag>) {
        if (studioList.isNotEmpty()) {
            if (studioList.size == 1) {
                binding.tvStudioSingle.text = studioList[0].name
                binding.tvStudioSingle.visibility = View.VISIBLE
                binding.flowStudios.visibility = View.GONE
            } else {
                binding.tvStudioSingle.visibility = View.GONE
                binding.flowStudios.removeAllViews()
                val adapter = object : BaseTagAdapter<StarStudioTag>() {
                    override fun getText(data: StarStudioTag): String {
                        return "${data.name}(${data.count})"
                    }

                    override fun getId(data: StarStudioTag): Long {
                        return data.studioId!!
                    }

                    override fun isDisabled(item: StarStudioTag): Boolean {
                        return false
                    }
                }
                adapter.setLayoutResource(R.layout.adapter_studio_tag)
                adapter.enableUnselect()
                adapter.setData(studioList)
                selectedTag?.let {
                    val tags = mutableListOf<StarStudioTag>()
                    tags.add(it)
                    adapter.setSelectedList(tags)
                }
                adapter.setOnItemSelectListener(object : OnItemSelectListener<StarStudioTag> {
                    override fun onSelectItem(tag: StarStudioTag) {
                        selectedTag = tag
                        onHeadActionListener?.onFilterStudio(tag.studioId!!)
                    }

                    override fun onUnSelectItem(tag: StarStudioTag) {
                        selectedTag = null
                        onHeadActionListener?.onCancelFilterStudio(tag.studioId!!)
                    }
                })
                adapter.bindFlowLayout(binding.flowStudios)
                binding.flowStudios.visibility = View.VISIBLE
            }
            binding.flowStudios.visibility = View.VISIBLE
        } else {
            binding.tvStudioSingle.visibility = View.GONE
            binding.flowStudios.visibility = View.GONE
        }
    }

    private fun bindOrders(binding: AdapterStarPhoneHeaderBinding, star: Star) {
        binding.ivOrderAdd.setOnClickListener {
            onHeadActionListener?.addStarToOrder(star)
        }
        binding.ivOrderDelete.setOnClickListener {
            ordersAdapter.toggleDeleteMode()
            ordersAdapter.notifyDataSetChanged()
        }
        binding.groupOrder.setOnClickListener { view: View? ->
            // collapse
            if (binding.ivOrderArrow.isSelected) {
                binding.ivOrderArrow.isSelected = false
                binding.ivOrderArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_grey_700_24dp)
                binding.rvOrders.visibility = View.GONE
            } else {
                binding.ivOrderArrow.isSelected = true
                binding.ivOrderArrow.setImageResource(R.drawable.ic_keyboard_arrow_up_grey_700_24dp)
                binding.rvOrders.visibility = View.VISIBLE
            }
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(binding.rvOrders.context, LinearLayoutManager.HORIZONTAL, false)
        if (mModel == null) {
            mModel = getActivityViewModel(binding.rvOrders.context, StarViewModel::class.java)
            mModel!!.ordersObserver.observe(
                binding.rvOrders.context as LifecycleOwner, Observer{ list ->
                    mBinding?.tvOrder?.text = list.size.toString()
                    // 添加order后用notifyDataSetChanged不知为何不管用，还得重新setAdapter才管用
                    ordersAdapter.onDeleteListener = object : StarOrdersAdapter.OnDeleteListener {
                        override fun onDeleteOrder(order: FavorStarOrder) {
                            mModel!!.deleteOrderOfStar(order.id!!, star.id!!)
                            mModel!!.loadStarOrders(star.id!!)
                        }
                    }
                    ordersAdapter.list = list
                    mBinding?.rvOrders?.adapter = ordersAdapter
                })
        }
        mModel!!.loadStarOrders(star.id!!)
    }

    private fun bindRatings(binding: AdapterStarPhoneHeaderBinding, star: Star) {
        binding.groupRating.setOnClickListener {
            // collapse
            if (binding.ivRatingArrow.isSelected) {
                binding.ivRatingArrow.isSelected = false
                binding.ivRatingArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_grey_700_24dp)
                binding.groupRatingSub.visibility = View.GONE
            } else {
                binding.ivRatingArrow.isSelected = true
                binding.ivRatingArrow.setImageResource(R.drawable.ic_keyboard_arrow_up_grey_700_24dp)
                binding.groupRatingSub.visibility = View.VISIBLE
            }
        }
        binding.tvRating.text = ""
        if (mRatingModel == null) {
            mRatingModel = getActivityViewModel(binding.groupRating.context, StarRatingViewModel::class.java)
            mRatingModel!!.ratingObserver.observe(
                (binding.groupRating.context as LifecycleOwner),
                Observer { rating: StarRating ->
                    showRatings(
                        binding,
                        rating
                    )
                }
            )
        }
        mRatingModel!!.loadStarRating(star.id!!)
    }

    private fun bindRelationships(binding: AdapterStarPhoneHeaderBinding, relationships: List<StarRelationship>) {
        binding.tvRelation.text = "${relationships.size.toString()}人"
        binding.rvRelation.layoutManager = LinearLayoutManager(binding.rvRelation.context, LinearLayoutManager.HORIZONTAL, false)
        if (binding.rvRelation.adapter == null) {
            binding.rvRelation.addItemDecoration(object : ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    if (position > 0) {
                        outRect.left = ScreenUtils.dp2px(10f)
                    }
                }
            })
            relationshipAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<StarRelationship> {
                override fun onClickItem(view: View, position: Int, data: StarRelationship) {
                    onHeadActionListener?.onClickRelationStar(data)
                }
            })
            relationshipAdapter.list = relationships
            binding.rvRelation.adapter = relationshipAdapter
        } else {
            relationshipAdapter.list = relationships
            relationshipAdapter.notifyDataSetChanged()
        }
        binding.groupRelation.setOnClickListener {
            // collapse
            if (binding.ivRelationArrow.isSelected) {
                binding.ivRelationArrow.isSelected = false
                binding.ivRelationArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_grey_700_24dp)
                binding.rvRelation.visibility = View.GONE
            } else {
                binding.ivRelationArrow.isSelected = true
                binding.ivRelationArrow.setImageResource(R.drawable.ic_keyboard_arrow_up_grey_700_24dp)
                binding.rvRelation.visibility = View.VISIBLE
            }
        }
    }

    private fun bindBasicInfo(binding: AdapterStarPhoneHeaderBinding, star: Star) {
    }

    fun showRatings(binding: AdapterStarPhoneHeaderBinding, rating: StarRating) {
        binding.tvRating.text = mRatingModel!!.getComplex()
        binding.starFace.setCheckNumber(rating.face)
        binding.tvFace.text = StarRatingUtil.getSubRatingValue(rating.face)
        binding.starBody.setCheckNumber(rating.body)
        binding.tvBody.text = StarRatingUtil.getSubRatingValue(rating.body)
        binding.starDk.setCheckNumber(rating.dk)
        binding.tvDk.text = StarRatingUtil.getSubRatingValue(rating.dk)
        binding.starSex.setCheckNumber(rating.sexuality)
        binding.tvSex.text = StarRatingUtil.getSubRatingValue(rating.sexuality)
        binding.starPassion.setCheckNumber(rating.passion)
        binding.tvPassion.text = StarRatingUtil.getSubRatingValue(rating.passion)
        binding.starVideo.setCheckNumber(rating.video)
        binding.tvVideo.text = StarRatingUtil.getSubRatingValue(rating.video)
        binding.starPrefer.setCheckNumber(rating.prefer)
        binding.tvPrefer.text = StarRatingUtil.getSubRatingValue(rating.prefer)
    }

    override fun onStarChanged(view: StarRatingView, checkedStar: Float) {
        val rateValue = StarRatingUtil.getSubRatingValue(checkedStar)
        when (view.id) {
            R.id.star_face -> {
                mRatingModel!!.rating!!.face = checkedStar
                mBinding!!.tvFace.text = rateValue
            }
            R.id.star_body -> {
                mRatingModel!!.rating!!.body = checkedStar
                mBinding!!.tvBody.text = rateValue
            }
            R.id.star_dk -> {
                mRatingModel!!.rating!!.dk = checkedStar
                mBinding!!.tvDk.text = rateValue
            }
            R.id.star_passion -> {
                mRatingModel!!.rating!!.passion = checkedStar
                mBinding!!.tvPassion.text = rateValue
            }
            R.id.star_video -> {
                mRatingModel!!.rating!!.video = checkedStar
                mBinding!!.tvVideo.text = rateValue
            }
            R.id.star_sex -> {
                mRatingModel!!.rating!!.sexuality = checkedStar
                mBinding!!.tvSex.text = rateValue
            }
            R.id.star_prefer -> {
                mRatingModel!!.rating!!.prefer = checkedStar
                mBinding!!.tvPrefer.text = rateValue
            }
        }
        mBinding!!.tvRating.text = mRatingModel!!.getComplex()
        mRatingModel!!.saveRating()
    }

    interface OnHeadActionListener {
        fun onClickRelationStar(relationship: StarRelationship)
        fun addStarToOrder(star: Star)
        fun onFilterStudio(studioId: Long)
        fun onCancelFilterStudio(studioId: Long)
        fun onAddTag()
        fun onDeleteTag(bean: Tag)
    }
}