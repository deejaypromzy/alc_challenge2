package com.dj.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

class TravelDeals extends RecyclerView.Adapter<TravelDeals.DealsViewHolder>  {

    private ArrayList<Database> travelDeals;
    private Context mContext;

    
    TravelDeals(Context context, ArrayList<Database> travelDeals) {
        this.travelDeals = travelDeals;
        this.mContext = context;
   }


    @NonNull
    @Override
    public DealsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DealsViewHolder(mContext, LayoutInflater.from(mContext).
                inflate(R.layout.deals_template, parent, false));
    }

    /**
     * Required method that binds the data to the viewholder.
     * @param holder The viewholder into which the data should be put.
     * @param position The adapter position.
     */
    @Override
    public void onBindViewHolder(DealsViewHolder holder, int position) {

        //Get the current sport
        Database currentDeal = travelDeals.get(position);

        //Bind the data to the views
        holder.bindTo(currentDeal);

    }


    /**
     * Required method for determining the size of the data set.
     * @return Size of the data set.
     */
    @Override
    public int getItemCount() {
        return travelDeals.size();
    }


    /**
     * ChildViewHolder class that represents each row of data in the RecyclerView
     */
    static class DealsViewHolder extends RecyclerView.ViewHolder
    {

        private final StorageReference UserProductImageRef;
        private DateFormat df;
        private Date date;        //Member Variables for the holder data
      //  private RatingBar price;
        private TextView city,place,desc,amt;
        private ImageView img;
        private Context mContext;
        private Database mcurrentDeal;
        private DatabaseReference mref;
        private FirebaseDatabase mfirebaseDatabase;
        private String userid;
        private String URL;
        private String getrole;
        private SharedPreferences sharedpreferences;

        DealsViewHolder(final Context context, View itemView) {
            super(itemView);

            //Initialize the views
            city = itemView.findViewById(R.id.city);
            place = itemView.findViewById(R.id.place);
            amt = itemView.findViewById(R.id.amt);
            desc = itemView.findViewById(R.id.desc);
            img = itemView.findViewById(R.id.img);
            mContext = context;

            mfirebaseDatabase = FirebaseDatabase.getInstance();
            mref = mfirebaseDatabase.getReference();
            UserProductImageRef = FirebaseStorage.getInstance().getReference();


                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SharedPreferences prefs = mContext.getSharedPreferences("myPrefs", MODE_PRIVATE);
                        if (prefs.getString("role", "0").equals("1")) {
                            Intent intent = new Intent(mContext, Edit_Travel_Deals.class);
                            intent.putExtra("city", mcurrentDeal.getCity());
                            intent.putExtra("amt", mcurrentDeal.getAmount());
                            intent.putExtra("place", mcurrentDeal.getPlace());
                            intent.putExtra("desc", mcurrentDeal.getDesc());
                            intent.putExtra("photoUrl", mcurrentDeal.getPhoto_url());

                            mContext.startActivity(intent);
                        }
                    }
                });


        }



            void bindTo(Database currentDeal){


                //Populate the textviews with data
            city.setText(currentDeal.getCity());
            amt.setText(String.format("GHC %s", currentDeal.getAmount()));
            place.setText(currentDeal.getPlace());
            desc.setText((currentDeal.getDesc()));


                //Get the current sport
            mcurrentDeal = currentDeal;

            GlideApp.with(mContext)
                    .load(currentDeal.
                            getPhoto_url())
                    .placeholder(R.drawable.no_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)
                    .into(img);
        }

    }
}
