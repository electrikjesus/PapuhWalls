package com.alexcruz.papuhwalls.Live;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.alexcruz.papuhwalls.R;

import java.util.ArrayList;

/**
 * Created by Daniel Huber on 22.12.2015.
 */

public class GridAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<WallItem> walls;

    public GridAdapter(Context context, ArrayList<WallItem> walls){
        super();
        this.mContext = context;
        this.walls = walls;
    }

    @Override
    public int getCount() {
        return walls.size();
    }

    @Override
    public WallItem getItem(int position) {
        return walls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItemChecked(int position, boolean checked){
        walls.get(position).setChecked(checked);
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View squareItem = convertView;

        WallsHolder holder;
        if (squareItem == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            squareItem = inflater.inflate(R.layout.wallpaper_item_manager, parent, false);
            holder = new WallsHolder(squareItem);
            squareItem.setTag(holder);
        } else {
            holder = (WallsHolder) squareItem.getTag();
        }

        boolean isChecked = walls.get(position).isChecked();
        holder.selection.setChecked(isChecked);

        if(walls.size() > 0) {

            final WallsHolder finalHolder = holder;
            new AsyncTask<Void, Void, Void>() {
                Bitmap bmp = null;

                @Override
                protected Void doInBackground(Void... params) {
                    bmp = BitmapFactory.decodeFile(walls.get(position).getPath());
                    bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth() / 2, bmp.getHeight() / 2, false);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    finalHolder.wall.setImageBitmap(bmp);
                }
            }.execute();
        }

        return squareItem;
    }

    public ArrayList<WallItem> getUncheckedItems(){
        ArrayList<WallItem> uncheckedItems = new ArrayList<>();

        for(WallItem wall : walls)
            if(!wall.isChecked())
                uncheckedItems.add(wall);

        return uncheckedItems;
    }

    class WallsHolder {
        ImageView wall;
        CheckBox selection;

        WallsHolder(View v) {
            wall = (ImageView) v.findViewById(R.id.wall);
            selection = (CheckBox) v.findViewById(R.id.selectionCb);
        }

    }

}
