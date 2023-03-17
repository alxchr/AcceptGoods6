package ru.abch.acceptgoods6;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class GoodsAdapter extends BaseAdapter {
    Context ctx;
    ArrayList<GoodsRow> goodsList;
    LayoutInflater lInflater;
    String TAG = "GoodsAdapter";
    public GoodsAdapter(Context context, ArrayList<GoodsRow> goods) {
        ctx = context;
        goodsList = goods;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public interface OnGoodsSelect {
        void onGoodsSelect(int position);
    }
    OnGoodsSelect goodsSelector;
    public void registerGoodsSelect(OnGoodsSelect ogs) {
        this.goodsSelector = ogs;
    }
    @Override
    public int getCount() {
        if (goodsList == null) return 0;
        else return goodsList.size();
    }

    public void update(ArrayList<GoodsRow> goodsList) {
        this.goodsList = goodsList;
        notifyDataSetChanged();
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
//        ((TextView) view.findViewById(R.id.tv_goods_brand)).setText(gr.brand);
        ((TextView) view.findViewById(R.id.tv_goods_qnt)).setText(String.valueOf(gr.qnt));
//        ((TextView) view.findViewById(R.id.tv_goods_cell)).setText(Config.formatCell(gr.cell));
        ((TextView) view.findViewById(R.id.tv_goods_cell)).setText(gr.cell);
        view.setTag(position);
        view.setOnClickListener(itemOnClickListener);
        view.setOnKeyListener(itemOnKeyListener);
        return view;
    }
    View.OnClickListener itemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "Position " + (int) view.getTag());
            if(goodsSelector != null) goodsSelector.onGoodsSelect((int) view.getTag());
        }
    };

    View.OnKeyListener itemOnKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            Log.d(TAG, "Position " + (int) view.getTag());
            if(goodsSelector != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                goodsSelector.onGoodsSelect((int) view.getTag());
            }
            return false;
        }
    };
}
