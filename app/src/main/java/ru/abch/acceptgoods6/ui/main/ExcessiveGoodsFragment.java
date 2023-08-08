package ru.abch.acceptgoods6.ui.main;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;

import com.bosphere.filelogger.FL;

import ru.abch.acceptgoods6.App;
import ru.abch.acceptgoods6.Cell;
import ru.abch.acceptgoods6.CheckCode;
import ru.abch.acceptgoods6.Config;
import ru.abch.acceptgoods6.Database;
import ru.abch.acceptgoods6.GoodsPosition;
import ru.abch.acceptgoods6.GoodsRow;
import ru.abch.acceptgoods6.MainActivity;
import ru.abch.acceptgoods6.R;
import ru.abch.acceptgoods6.ScannedCode;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExcessiveGoodsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExcessiveGoodsFragment extends Fragment {

    public ExcessiveGoodsFragment() {
        // Required empty public constructor
    }
    EditText etArticle, etQnt, etCell;
    TextView tvGoodsInfo;
    Button btYes, btNo;
    AlertDialog.Builder adbGoods;
    int qnt, previousState;
//    LiveData<ScannedCode> scannedData;
    Cell cellIn;
    String goodsId;
    static final String TAG = "ExcessiveGoodsFragment";
    // TODO: Rename and change types and number of parameters
    public static ExcessiveGoodsFragment newInstance(int previousState) {
        ExcessiveGoodsFragment fragment = new ExcessiveGoodsFragment();
        Bundle args = new Bundle();
        fragment.previousState = previousState;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        etArticle.requestFocus();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_excessive_goods, container, false);
        etArticle = view.findViewById(R.id.et_excessive_goods_article);
        tvGoodsInfo = view.findViewById(R.id.tv_excessive_goods_info);
        btNo = view.findViewById(R.id.bt_decline);
        btYes = view.findViewById(R.id.bt_confirm);
        etQnt = view.findViewById(R.id.et_qnt);
        etCell = view.findViewById(R.id.et_cell);
        etArticle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "On text changed =" + charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input;
                int entIndex;
                if(editable.length() > 2) {
                    input = editable.toString();
                    if (input.contains("\n") && input.indexOf("\n") == 0) {
                        input = input.substring(1);
                    }
                    if (input.contains("\n") && input.indexOf("\n") > 0) {
                        entIndex = input.indexOf("\n");
                        input = input.substring(0, entIndex);
                        if (input.length() > 2) {
                            etArticle.setEnabled(false);
                            GoodsPosition[] foundGoods = Database.searchGoods(input);
                            if (foundGoods == null) {
                                Log.d(TAG, "Nothing found for " + input);
                                MainActivity.say(getResources().getString(R.string.enter_again));
                            } else {
                                if(foundGoods.length == 1) {
                                    Log.d(TAG, "Found " + foundGoods[0].getDescription());
                                    GoodsPosition gp = foundGoods[0];
                                    int q = (gp.getQnt() == 0) ? 1 : gp.getQnt();
                                    qnt = q;
                                    etQnt.setText(String.valueOf(q));
                                    String info = gp.getArticle() + " " + gp.getDescription();
                                    tvGoodsInfo.setText(info);
                                    etArticle.setEnabled(false);
                                    etQnt.setEnabled(true);
                                    etQnt.requestFocus();
                                    etQnt.setSelection(etQnt.getText().length());
                                    goodsId = gp.getId();
                                } else {
                                    final String[]goodsDescriptions;
                                    goodsDescriptions = new String[foundGoods.length];
                                    for (int j = 0; j < foundGoods.length; j++) {
                                        goodsDescriptions[j] = foundGoods[j].getDescription();
                                    }
                                    Log.d(TAG, "Found " + foundGoods.length + " positions");
                                    adbGoods = new AlertDialog.Builder(requireActivity());
                                    adbGoods.setTitle(R.string.goods_choice).setItems(goodsDescriptions, (dialog, which) -> {
                                        Log.d(TAG, "Index = " + which + " goods=" + goodsDescriptions[which]);
                                        GoodsPosition gp = foundGoods[which];
                                        int q = (gp.getQnt() == 0) ? 1 : gp.getQnt();
                                        qnt = q;
                                        etQnt.setText(String.valueOf(q));
                                        String info = gp.getArticle() + " " + gp.getDescription();
                                        tvGoodsInfo.setText(info);
                                        etArticle.setEnabled(false);
                                        etQnt.setEnabled(true);
                                        etQnt.requestFocus();
                                        etQnt.setSelection(etQnt.getText().length());
                                        goodsId = gp.getId();
                                    }).create().show();
                                }
                            }
                            etArticle.setEnabled(true);
                        } else {
                            MainActivity.say(getResources().getString(R.string.enter_again));
                        }
                        etArticle.setText("");
                    }
                }
            }
        });
        etCell.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "On text changed =" + charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input, cellName, cell;
                int entIndex;
                if(editable.length() > 1) {
                    input = editable.toString();
                    if (input.contains("\n") && input.indexOf("\n") == 0) {
                        input = input.substring(1);
                    }
                    if (input.contains("\n") && input.indexOf("\n") > 0) {
                        entIndex = input.indexOf("\n");
                        input = input.substring(0, entIndex);
                        if (CheckCode.checkCellStr(input)) {  //manual cell input
                            int prefix, suffix;
                            String result;
                            prefix = Integer.parseInt(input.substring(0, input.indexOf(".")));
                            suffix = Integer.parseInt(input.substring(input.indexOf(".") + 1));
                            cellName = String.format("%02d",prefix) + String.format("%03d",suffix);
                            result = App.warehouse.storeCode + cellName + "000";
                            int [] resDigit = new int[12];
                            for (int i = 0; i < 12; i++) {
                                resDigit[i] = Integer.parseInt(result.substring(i, i+1));
                            }
                            int e = (resDigit[1] + resDigit[3] + resDigit[5] +resDigit[7] + resDigit[9] + resDigit[11]) * 3;
                            int o = resDigit[0] + resDigit[2] + resDigit[4] +resDigit[6] + resDigit[8] + resDigit[10];
                            String r = String.valueOf(o+e);
                            int c = 10 - Integer.parseInt(r.substring(r.length() - 1));
                            if (c == 10) c = 0;
                            cell = result + c;
                            FL.d(TAG,"Manual input =" + input + " cell =" + cell + " cell name =" + cellName);
                            cellIn = Database.getCellByName(cellName);
                            if(cellIn != null) {
                                etCell.setText(cellIn.descr);
                            } else {
                                etCell.setText("");
                                MainActivity.say(getResources().getString(R.string.wrong_cell));
                            }
                        } else {
                            etCell.setText("");
                            MainActivity.say(getResources().getString(R.string.wrong_cell));
                        }
                    }
                }
            }
        });
        etCell.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) {
                ((EditText) v).setText("");
            }
        });
        btNo.setOnClickListener(v -> {
            App.state = previousState;
            ((MainActivity) requireActivity()).gotoMainFragment();
        });
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        /*
        scannedData = MainActivity.mViewModel.getScannedEGFragment();
        scannedData.observe(getViewLifecycleOwner(), scannedCode -> {
            if (scannedCode != null) {
                FL.d(TAG, "onChanged() data " + scannedCode.data + " id " + scannedCode.codeId);
                MainActivity.mViewModel.scannedEGFragment.postValue(null);
                FL.d(TAG,"Purge scan");
                if (App.state == App.SELECTEXCESSIVEGOODS) {
                    if (scannedCode.codeId.equals("b")) {
                        FL.d(TAG, "Scan code39 =" + scannedCode.data);
                        if (CheckCode.checkGoods39(scannedCode.data) && scannedCode.data.length() >= 11 && scannedCode.data.contains(".")) {
                            goodsId = scannedCode.data.substring(1, 10).replaceAll("\\.", " ");
                            String goodsQnt = scannedCode.data.substring(10).replaceAll("\\.", "");
                            int unitQnt = Integer.parseInt(goodsQnt, 36);
                            FL.d(TAG, "Goods id ='" + goodsId + "' goods qnt ='" + goodsQnt + "' " + unitQnt);
                            GoodsPosition gp = Database.searchGoodsById(goodsId);
                            if(gp != null) {
                                String info = gp.getArticle() + " " + gp.getDescription();
                                tvGoodsInfo.setText(info);
                                etArticle.setEnabled(false);
                                etQnt.setText(String.valueOf(unitQnt));
                                etQnt.requestFocus();
                                etQnt.setSelection(etQnt.getText().length());
                            } else {
                                MainActivity.say(getResources().getString(R.string.wrong_goods));
                                goodsId = null;
                            }
                        } else {
                            addScannedGoods(scannedCode.data);
                        }
                    } else
                    if (scannedCode.codeId.equals("c") && scannedCode.data.length() == 11) {
//                        Log.d(TAG, "Scan UPC =" + scannedCode.data);
                        //Rebuild UPC check digit
                        String res = scannedCode.data;
                        int[] resDigit = new int[11];
                        for (int i = 0; i < 11; i++) {
                            resDigit[i] = Integer.parseInt(res.substring(i, i + 1));
                        }
                        int e = resDigit[1] + resDigit[3] + resDigit[5] + resDigit[7] + resDigit[9];
                        int o = resDigit[0] + resDigit[2] + resDigit[4] + resDigit[6] + resDigit[8] + resDigit[10];
                        o *= 3;
                        String r = String.valueOf(o + e);
                        int c = 10 - Integer.parseInt(r.substring(r.length() - 1));
                        if (c == 10) c = 0;
                        res = res + c;
                        FL.d(TAG, "Rebuilt UPC code =" + res);
                        //probably goods barcode
                        addScannedGoods(res);
                    } else
                    if (scannedCode.codeId.equals("d") && scannedCode.data.length() == 12) {
//                        Log.d(TAG, "Scan EAN13 =" + scannedCode.data);
                        //Rebuild EAN13 check digit
                        String res = scannedCode.data;
                        int[] resDigit = new int[12];
                        for (int i = 0; i < 12; i++) {
                            resDigit[i] = Integer.parseInt(res.substring(i, i + 1));
                        }
                        int e = (resDigit[1] + resDigit[3] + resDigit[5] + resDigit[7] + resDigit[9] + resDigit[11]) * 3;
                        int o = resDigit[0] + resDigit[2] + resDigit[4] + resDigit[6] + resDigit[8] + resDigit[10];
                        String r = String.valueOf(o + e);
                        int c = 10 - Integer.parseInt(r.substring(r.length() - 1));
                        if (c == 10) c = 0;
                        res = res + c;
                        FL.d(TAG, "Rebuilt EAN13 code " + res);
                        cellIn = res.startsWith(App.storeCode[App.getStoreIndex()]) || res.startsWith("1900") && App.getStoreIndex() == 3? Database.getCellByName(Config.getCellName(res)) : null;
                        if (cellIn != null) {
                            FL.d(TAG, "Scanned cell " + cellIn.descr);
                            etCell.setText(cellIn.descr);
                        } else {
                            //probably goods barcode
                            addScannedGoods(res);
                        }
                    } else {
                        //all other codes
                        addScannedGoods(scannedCode.data);
                    }
                }

            }

        });

         */
        btYes.setOnClickListener(v -> {
            String sQnt = etQnt.getText().toString();
            if (sQnt.length() > 0) {
                qnt = Integer.parseInt(sQnt);
            } else {
                qnt = 0;
            }
            String cellName, input = etCell.getText().toString();
//            Log.d(TAG, "Cell " + input);
//            if (CheckCode.checkCellStr(input)) {  //manual cell input
            /*
                int prefix, suffix;
                prefix = Integer.parseInt(input.substring(0, input.indexOf(".")));
                suffix = Integer.parseInt(input.substring(input.indexOf(".") + 1));
                cellName = String.format("%02d",prefix) + String.format("%03d",suffix);
                cellIn = Database.getCellByName(cellName);

             */
                if(cellIn != null) {
                    etCell.setText(cellIn.descr);
                    if(goodsId != null && qnt > 0 && qnt < 10000) {
                        Cell lost = Database.getCellByName(getResources().getString(R.string.lost_cell_name));
                        if (lost != null) {
                            Database.addExcessiveGoods(App.getStoreMan(), goodsId, "",
                                    qnt, cellIn.id, MainActivity.getCurrentTime(), lost.id);
                            ((MainActivity) requireActivity()).uploadGoods();
                        } else {
                            FL.e(TAG,"No lost goods cell");
                        }
                        MainActivity.mViewModel.loadGoodsData();
                        App.state = previousState;
                        FL.d(TAG, "gotoMainFragment() state " + App.state);
                        ((MainActivity) requireActivity()).gotoMainFragment();
                    } else {
                        MainActivity.say(getResources().getString(R.string.check_info));
                    }
                } else {
                    etCell.setText("");
                    MainActivity.say(getResources().getString(R.string.wrong_cell));
                }
//            } else {
//                etCell.setText("");
//                MainActivity.say(getResources().getString(R.string.check_info));
//            }
//            Log.d(TAG, "Goods id " + goodsId + " cellIn " + cellIn.descr + " qnt " + qnt);

        });
    }
    private void addScannedGoods(String barcode) {
//        GoodsRow scannedGoods = Database.getGoodsRow(barcode);
        GoodsRow scannedGoods = App.getPackMode()? Database.getPackRow(barcode)
                : Database.getGoodsRow(barcode);
        if (scannedGoods != null) {
            String info = scannedGoods.article + " " + scannedGoods.description;
            tvGoodsInfo.setText(info);
            etArticle.setEnabled(false);
            etQnt.setEnabled(true);
            int q = (scannedGoods.qnt == 0) ? 1 : scannedGoods.qnt;
            qnt = q;
            etQnt.setText(String.valueOf(q));
            etQnt.requestFocus();
            etQnt.setSelection(etQnt.getText().length());
            goodsId = scannedGoods.id;
        } else {
            MainActivity.say(getResources().getString(R.string.wrong_goods));
            FL.d(TAG, "Goods code " + barcode + " not found");
        }
    }
    public void processScan(ScannedCode scannedCode) {
        if (scannedCode != null) {
            if (App.state == App.SELECTEXCESSIVEGOODS) {
                if (scannedCode.codeId.equals("b")) {
                    FL.d(TAG, "Scan code39 =" + scannedCode.data);
                    if (CheckCode.checkGoods39(scannedCode.data) && scannedCode.data.length() >= 11 && scannedCode.data.contains(".")) {
                        goodsId = scannedCode.data.substring(1, 10).replaceAll("\\.", " ");
                        String goodsQnt = scannedCode.data.substring(10).replaceAll("\\.", "");
                        int unitQnt = Integer.parseInt(goodsQnt, 36);
                        FL.d(TAG, "Goods id ='" + goodsId + "' goods qnt ='" + goodsQnt + "' " + unitQnt);
                        GoodsPosition gp = Database.searchGoodsById(goodsId);
                        if(gp != null) {
                            String info = gp.getArticle() + " " + gp.getDescription();
                            tvGoodsInfo.setText(info);
                            etArticle.setEnabled(false);
                            etQnt.setText(String.valueOf(unitQnt));
                            etQnt.requestFocus();
                            etQnt.setSelection(etQnt.getText().length());
                        } else {
                            MainActivity.say(getResources().getString(R.string.wrong_goods));
                            goodsId = null;
                        }
                    } else {
                        addScannedGoods(scannedCode.data);
                    }
                } else
                if (scannedCode.codeId.equals("c") && scannedCode.data.length() == 11) {
//                        Log.d(TAG, "Scan UPC =" + scannedCode.data);
                    //Rebuild UPC check digit
                    String res = scannedCode.data;
                    int[] resDigit = new int[11];
                    for (int i = 0; i < 11; i++) {
                        resDigit[i] = Integer.parseInt(res.substring(i, i + 1));
                    }
                    int e = resDigit[1] + resDigit[3] + resDigit[5] + resDigit[7] + resDigit[9];
                    int o = resDigit[0] + resDigit[2] + resDigit[4] + resDigit[6] + resDigit[8] + resDigit[10];
                    o *= 3;
                    String r = String.valueOf(o + e);
                    int c = 10 - Integer.parseInt(r.substring(r.length() - 1));
                    if (c == 10) c = 0;
                    res = res + c;
                    FL.d(TAG, "Rebuilt UPC code =" + res);
                    //probably goods barcode
                    addScannedGoods(res);
                } else
                if (scannedCode.codeId.equals("d") && scannedCode.data.length() == 12) {
//                        Log.d(TAG, "Scan EAN13 =" + scannedCode.data);
                    //Rebuild EAN13 check digit
                    String res = scannedCode.data;
                    int[] resDigit = new int[12];
                    for (int i = 0; i < 12; i++) {
                        resDigit[i] = Integer.parseInt(res.substring(i, i + 1));
                    }
                    int e = (resDigit[1] + resDigit[3] + resDigit[5] + resDigit[7] + resDigit[9] + resDigit[11]) * 3;
                    int o = resDigit[0] + resDigit[2] + resDigit[4] + resDigit[6] + resDigit[8] + resDigit[10];
                    String r = String.valueOf(o + e);
                    int c = 10 - Integer.parseInt(r.substring(r.length() - 1));
                    if (c == 10) c = 0;
                    res = res + c;
                    FL.d(TAG, "Rebuilt EAN13 code " + res);
                    cellIn = res.startsWith(App.warehouse.storeCode) || res.startsWith("1900") && App.warehouse.id == "    12SPR"? Database.getCellByName(Config.getCellName(res)) : null;
                    if (cellIn != null) {
                        FL.d(TAG, "Scanned cell " + cellIn.descr);
                        etCell.setText(cellIn.descr);
                    } else {
                        //probably goods barcode
                        addScannedGoods(res);
                    }
                } else {
                    //all other codes
                    addScannedGoods(scannedCode.data);
                }
            }
                /*
                MainActivity.mViewModel.scannedData.postValue(null);
                FL.d(TAG,"Purge scan");

                 */
        }
    }
}