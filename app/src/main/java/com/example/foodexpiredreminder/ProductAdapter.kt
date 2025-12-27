package com.example.foodexpiredreminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductAdapter(
    private var productListFull: MutableList<Product>,
    private val onEditClick: (product: Product, position: Int) -> Unit,
    private val onDeleteClick: (product: Product, position: Int) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(), Filterable {

    private var productListFiltered: MutableList<Product> = mutableListOf()
    private var currentCategoryFilter: ProductType? = null
    private var currentSearchQuery: String = ""

    init {
        productListFiltered.addAll(productListFull)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productListFiltered[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int {
        return productListFiltered.size
    }

    fun updateData(newProductList: List<Product>) {
        // Create a temporary copy to prevent issues when the source list is the same instance
        val tempList = newProductList.toList()
        this.productListFull.clear()
        this.productListFull.addAll(tempList)
        // The filter will be applied, which in turn updates productListFiltered and notifies the adapter.
        filter.filter(currentSearchQuery)
    }

    fun setCategoryFilter(category: ProductType?) {
        currentCategoryFilter = category
        filter.filter(currentSearchQuery)
    }

    class ProductViewHolder(
        itemView: View,
        private val onEditClick: (product: Product, position: Int) -> Unit,
        private val onDeleteClick: (product: Product, position: Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val buttonEdit: ImageView = itemView.findViewById(R.id.buttonEdit)
        private val buttonDelete: ImageView = itemView.findViewById(R.id.buttonDelete)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productType: TextView = itemView.findViewById(R.id.productType)
        private val productQuantity: TextView = itemView.findViewById(R.id.productQuantity)
        private val purchaseDate: TextView = itemView.findViewById(R.id.purchaseDate)
        private val expiryDate: TextView = itemView.findViewById(R.id.expiryDate)

        private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        fun bind(product: Product) {
            productName.text = product.name
            productType.text = "Jenis: ${product.type.name.lowercase().replaceFirstChar { it.uppercase() }}"
            productQuantity.text = "Jumlah: ${product.quantity}"
            purchaseDate.text = "Tgl Beli: ${dateFormat.format(Date(product.purchaseDate))}"
            expiryDate.text = "Kadaluwarsa: ${dateFormat.format(Date(product.expiryDate))}"

            buttonEdit.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onEditClick(product, currentPosition)
                }
            }

            buttonDelete.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onDeleteClick(product, currentPosition)
                }
            }

            val statusColor = when (product.status) {
                ProductStatus.AMAN -> R.color.status_safe
                ProductStatus.HAMPIR_KADALUWARSA -> R.color.status_warning
                ProductStatus.KADALUWARSA -> R.color.status_expired
            }
            statusIndicator.setBackgroundColor(ContextCompat.getColor(itemView.context, statusColor))
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                currentSearchQuery = constraint?.toString()?.lowercase(Locale.getDefault()) ?: ""

                var filteredList: List<Product> = productListFull

                currentCategoryFilter?.let { category ->
                    filteredList = filteredList.filter { it.type == category }
                }

                if (currentSearchQuery.isNotEmpty()) {
                    filteredList = filteredList.filter {
                        it.name.lowercase(Locale.getDefault()).contains(currentSearchQuery)
                    }
                }

                val sortedList = filteredList.sortedBy { it.expiryDate }

                val results = FilterResults()
                results.values = sortedList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                productListFiltered.clear()
                (results?.values as? List<Product>)?.let { productListFiltered.addAll(it) }
                notifyDataSetChanged()
            }
        }
    }
}