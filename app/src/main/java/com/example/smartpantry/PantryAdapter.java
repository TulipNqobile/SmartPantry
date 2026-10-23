package com.example.smartpantry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Custom adapter that shows each PantryItem as a card in the pantry list. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items;
    private final Listener listener;
    private boolean highlightExpiring = true;

    public PantryAdapter(List<PantryItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setHighlightExpiring(boolean highlightExpiring) {
        this.highlightExpiring = highlightExpiring;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final PantryItem item = items.get(position);
        holder.name.setText(item.getName());
        holder.qty.setText(formatQty(item.getQuantity()) + " " + item.getUnit());

        String expiry = item.getExpiryDate();
        if (expiry == null || expiry.trim().isEmpty()) {
            holder.expiry.setText("No expiry date");
            holder.expiry.setTextColor(holder.qty.getCurrentTextColor());
        } else if (highlightExpiring && isExpiringSoon(expiry)) {
            holder.expiry.setText("Expires soon: " + expiry);
            holder.expiry.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            holder.expiry.setText("Expires: " + expiry);
            holder.expiry.setTextColor(holder.qty.getCurrentTextColor());
        }

        holder.itemView.setOnClickListener(v -> listener.onEdit(item));
        holder.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Shows 3 instead of 3.0 for whole numbers. */
    public static String formatQty(double q) {
        if (q == Math.floor(q)) {
            return String.valueOf((long) q);
        }
        return String.valueOf(q);
    }

    private boolean isExpiringSoon(String date) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
            long days = (d.getTime() - System.currentTimeMillis()) / (24L * 60 * 60 * 1000);
            return days <= 3;
        } catch (ParseException e) {
            return false;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, qty, expiry;
        ImageButton delete;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.text_name);
            qty = v.findViewById(R.id.text_qty);
            expiry = v.findViewById(R.id.text_expiry);
            delete = v.findViewById(R.id.button_delete);
        }
    }
}
