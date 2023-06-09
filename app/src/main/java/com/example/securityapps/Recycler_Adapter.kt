package com.example.securityapps

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
//RecyclerAdapter Class
class Recycler_Adapter: RecyclerView.Adapter<Recycler_Adapter.ViewHolder>() {

    // these are titles array
    private var title=arrayOf("Images","Documents","Recordings","Security")
    private var details = arrayOf(".jpg, .png and .gif formats",".pdf , .docx and .doc format","All downloaded Recordings not live","Mobile Apps to transfer","Protect your Device","Miscellaneous","Videos to be sent","Sending .txt and other file formats","Sending html pages")
    private var images= intArrayOf(R.drawable.transfer,R.drawable.transfer,R.drawable.transfer,R.drawable.transfer,R.drawable.transfer,R.drawable.transfer,R.drawable.transfer,R.drawable.transfer,R.drawable.transfer,R.drawable.transfer)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Recycler_Adapter.ViewHolder {
      //create the View Holder from the View Class Inner Class
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: Recycler_Adapter.ViewHolder, position: Int) {
      holder.itemTitle.text=title[position]

      holder.itemImage.setImageResource(images[position])

    }

    override fun getItemCount(): Int {
        return title.size
    }
    @SuppressLint("CutPasteId")
    //For the cardLayout ViewHolder InnerClass
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
    {
        var itemImage: ImageView
        var itemTitle: TextView

        init{
            itemImage = itemView.findViewById(R.id.item_constraint)
            itemTitle= itemView.findViewById(R.id.item_title)


            itemView.setOnClickListener{
                val position: Int = adapterPosition
                when(position){
                    0->{
                        val intent = Intent(itemView.context, FileImages::class.java)
                        itemView.context.startActivity(intent)

                    }
                    1->{
                        val intent1= Intent(itemView.context,Documents::class.java)
                        itemView.context.startActivity(intent1)
                    }
                    2->{
                        val intent1= Intent(itemView.context,Recordings::class.java)
                        itemView.context.startActivity(intent1)
                    }

                    3->{
                        val intent1= Intent(itemView.context,SecurityMeasures::class.java)
                        itemView.context.startActivity(intent1)
                    }




                    else ->{
                        Toast.makeText(itemView.context,"you clicked on ${title[position]} and ",Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
    }

}