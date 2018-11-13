package com.fallendeductionist.evaluacion4;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SolicitudeAdapter extends RecyclerView.Adapter<SolicitudeAdapter.ViewHolder> {

    private List<Solicitude>solicitudes;

    public SolicitudeAdapter(){
        this.solicitudes = new ArrayList<>();
    }

    public void setSolicitudes(List<Solicitude>solicitudes){
        this.solicitudes = solicitudes;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView voucherImage;
        public TextView solicitudeTitle;
        public TextView solicitudeEmail;
        public TextView solicitudeNavigation;

        public ViewHolder(View itemView){
            super(itemView);
            voucherImage = itemView.findViewById(R.id.solicitude_voucher);
            solicitudeTitle = itemView.findViewById(R.id.solicitude_title);
            solicitudeEmail = itemView.findViewById(R.id.email_card);
            solicitudeNavigation = itemView.findViewById(R.id.coordinates);
        }
    }

    @Override
    public SolicitudeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.solicitude_example, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SolicitudeAdapter.ViewHolder viewHolder, int position) {

        Solicitude solicitude = this.solicitudes.get(position);
        String url = solicitude.getImage();

        Picasso.with(viewHolder.itemView.getContext()).load(url).into(viewHolder.voucherImage);
        viewHolder.solicitudeTitle.setText(solicitude.getTitle());
        Picasso.with(viewHolder.itemView.getContext()).setLoggingEnabled(true);
        viewHolder.solicitudeEmail.setText("De: " + solicitude.getEmail());
        viewHolder.solicitudeNavigation.setText(solicitude.getNavigation());

    }

    @Override
    public int getItemCount() {
        return this.solicitudes.size();
    }

}
