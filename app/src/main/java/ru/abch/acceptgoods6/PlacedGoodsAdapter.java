package ru.abch.acceptgoods6;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class PlacedGoodsAdapter extends BaseAdapter {
    ArrayList<GoodsRow> goodsList;
    Context ctx;
    LayoutInflater lInflater;
    String TAG = "PlacedGoodsAdapter";
    public PlacedGoodsAdapter(Context context, ArrayList<GoodsRow> goods) {
        ctx = context;
        goodsList = goods;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        if (goodsList == null) return 0;
        else return goodsList.size();
    }

    @Override
    public Object getItem(int i) {
        return goodsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.goods_item, parent, false);
        }
        GoodsRow gr = (GoodsRow) getItem(position);
        String goods = gr.article + " " + gr.description;
        if (goods.length() > 24) goods = goods.substring(0,24);
        ((TextView) view.findViewById(R.id.tv_goods_descr)).setText(goods);
        ((TextView) view.findViewById(R.id.tv_goods_brand)).setText(gr.brand);
        ((TextView) view.findViewById(R.id.tv_goods_qnt)).setText(String.valueOf(gr.qnt));
        ((TextView) view.findViewById(R.id.tv_goods_cell)).setText(gr.cell);
        return view;
    }
}
