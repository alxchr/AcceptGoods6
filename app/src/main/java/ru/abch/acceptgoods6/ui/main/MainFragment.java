package ru.abch.acceptgoods6.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bosphere.filelogger.FL;

import java.util.ArrayList;
import java.util.Objects;

import ru.abch.acceptgoods6.App;
import ru.abch.acceptgoods6.Cell;
import ru.abch.acceptgoods6.CheckCode;
import ru.abch.acceptgoods6.Config;
import ru.abch.acceptgoods6.Database;
import ru.abch.acceptgoods6.GoodsAdapter;
import ru.abch.acceptgoods6.GoodsRow;
import ru.abch.acceptgoods6.MainActivity;
import ru.abch.acceptgoods6.R;
import ru.abch.acceptgoods6.ScannedCode;

public class MainFragment extends Fragment {
    ListView lvTaskList;
    EditText etBox;
    private static final String TAG = "MainFragment";
    GoodsAdapter goodsAdapter;
    public static MainFragment newInstance() {
        return new MainFragment();
    }
    Cell outBox;
    LiveData<ArrayList<GoodsRow>> goodsList;
//    LiveData<ScannedCode> scannedData;
    boolean boxSaved = false;
    int selectedPosition = -1;
    private GoodsRow currentGoods = null;
    ProgressBar pbWaitBox;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        lvTaskList = view.findViewById((R.id.lv_task_list));
        pbWaitBox = view.findViewById(R.id.pb_wait_box);
        etBox = view.findViewById(R.id.et_box);
        etBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(boxSaved) {
                    boxSaved = false;
                    etBox.setText("");
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Log.d(TAG, "On text changed =" + charSequence);
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
                            outBox = Database.getCellByName(cellName);
                            if(outBox != null) {
                                Database.purgeSentData(true);   //no current box, all sent data must be cleared
                                etBox.setText(outBox.descr);
                                App.setCurrentBox(outBox);
                                etBox.setEnabled(false);
                                ((MainActivity) requireActivity()).refreshData();
                            } else {
                                etBox.setText("");
                                MainActivity.say(getResources().getString(R.string.wrong_box));
                            }
                        } if(!input.isEmpty()) {
//                            Log.d(TAG,"Pallette num " + input);
                            App.setCurrentPackNum(input);
                            App.setPackMode(true);
                            etBox.setText(input);
                            etBox.setEnabled(false);
                            ((MainActivity) requireActivity()).refreshData();
                        } else {
                            etBox.setText("");
                            MainActivity.say(getResources().getString(R.string.wrong_cell));
                        }
                    }

                }
            }
        });
        return view;
    }

    public void showGoodsList (ArrayList<GoodsRow> goodsList) {
        goodsAdapter = new GoodsAdapter(getActivity(), goodsList);
        goodsAdapter.registerGoodsSelect(position -> ((MainActivity) requireActivity()).gotoGoodsFragment(goodsList.get(position), 0));
        lvTaskList.setAdapter(goodsAdapter);
        lvTaskList.requestFocus();
        lvTaskList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedPosition = position;
                etBox.setEnabled(false);
            }

            public void onNothingSelected(AdapterView<?> parent) {
                selectedPosition = -1;
            }
        });

        lvTaskList.setOnKeyListener((view, i, keyEvent) -> {
            if(selectedPosition >= 0 &&
                    keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                ((MainActivity) requireActivity()).gotoGoodsFragment(goodsList.get(selectedPosition), 0);
            }
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<GoodsRow> list = MainActivity.mViewModel.getGoodsData().getValue();
        outBox = App.getCurrentBox();
        String savedBoxDescr = (App.getCurrentBox() == null)? "" : App.getCurrentBox().descr;
        if(App.getPackMode() && !App.getCurrentPackNum().isEmpty()) {
            savedBoxDescr = App.getCurrentPackNum();
        }
        boxSaved = savedBoxDescr.length() > 0;
//        etBox.setText(savedBoxDescr);
        if(!Database.getPlacedList().isEmpty() && Database.getTaskList().isEmpty()) {
            ((MainActivity) requireActivity()).showAdbPlaced();
        }
        goodsList = MainActivity.mViewModel.getGoodsData();
        ((MainActivity) requireActivity()).loadGoodsData();
    }
    public void showWait(boolean show) {
        if(show) {
            lvTaskList.setVisibility(View.GONE);
            pbWaitBox.setVisibility(View.VISIBLE);
        } else {
            pbWaitBox.setVisibility(View.GONE);
            lvTaskList.setVisibility(View.VISIBLE);
        }
    }

    public Runnable showProgress = () -> showWait(true);
    public Runnable hideProgress = new Runnable() {
        @Override
        public void run() {
            showWait(false);
            goodsList.observe(getViewLifecycleOwner(), goodsRows -> {
                if(goodsRows.size() > 0) {
                    String sbd = (App.getCurrentBox() == null)? "" : App.getCurrentBox().descr;
                    if(App.getPackMode() && !App.getCurrentPackNum().isEmpty()) {
                        sbd = App.getCurrentPackNum();
                    }
                    boxSaved = sbd.length() > 0;
                    etBox.setText(sbd);
                    etBox.setEnabled(false);
                    App.state = App.SELECTGOODS;
                } else {
                    if(App.state != App.START) {
                        MainActivity.say(getResources().getString(R.string.empty_box_tts));
                        ((MainActivity) requireActivity()).uploadGoods();
                    }
                    etBox.setEnabled(true);
                    etBox.setText("");
                    etBox.requestFocus();
                    boxSaved = false;
                    App.state = App.MAINCYCLE;
                    App.setCurrentBox(null);
                    FL.d(TAG,"Observe: empty goodsRows");
                }
                showGoodsList(goodsRows);
            });
        }
    };
    public void processScan(ScannedCode scannedCode) {
        if(scannedCode != null) {
            if (App.state == App.START || App.state == App.MAINCYCLE || App.state == App.SELECTGOODS) {
                if (scannedCode.data.length() > 0 && scannedCode.codeId.length() > 0) {
                    if (App.state == App.START) {
                        MainActivity.say(getResources().getString(R.string.storeman_number_tts));
                    } else {
                        if (scannedCode.codeId.equals("d") && scannedCode.data.length() == 12) {
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
                            switch (App.state) {
                                case App.MAINCYCLE:
                                    /*
                                    outBox = res.startsWith(App.storeCode[App.getStoreIndex()])  || res.startsWith("1900") && App.getStoreIndex() == 3?
                                            Database.getCellByName(Config.getCellName(res)) : null;

                                     */
                                    String code = App.warehouse == null? "aa" : App.warehouse.storeCode;
                                    assert code != null;
                                    outBox = res.startsWith(code) || res.startsWith("1900") && App.getStoreId().equals("    12SPR")?
                                            Database.getCellByName(Config.getCellName(res)) : null;
                                    if (outBox == null) {
                                        MainActivity.say(getResources().getString(R.string.wrong_box));
                                    } else {
                                        Database.purgeSentData(true);   //no current box, all sent data must be cleared
                                        App.state = App.SELECTGOODS;
                                        App.setCurrentBox(outBox);
                                        etBox.setText(outBox.descr);
                                        etBox.setEnabled(false);
                                        App.setPackMode(false);             //process box
                                        ((MainActivity) requireActivity()).refreshData();
                                    }
                                    break;
                                case App.SELECTGOODS:
                                    currentGoods = Database.getGoodsRow(res);
                                    if (currentGoods == null) {
                                        MainActivity.say(getResources().getString(R.string.wrong_goods));
                                        FL.d(TAG, "Wrong goods code " + res);
                                    } else {
                                        int unitQnt = Database.getBarcodeQnt(res);
                                        ((MainActivity) requireActivity()).gotoGoodsFragment(currentGoods, unitQnt);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else if(scannedCode.codeId.equals("b") && App.state == App.MAINCYCLE) {
                            Log.d(TAG, "Pack label scanned " + scannedCode.data);
                            if(scannedCode.data.startsWith("UI") && scannedCode.data.length() == 11) {
                                String packId = scannedCode.data.substring(2).replaceAll("\\."," ");
                                Log.d(TAG, "Pack id scanned " + packId);
                                App.setPackMode(true);  //process pack
                                App.setCurrentPackId(packId);
                                ((MainActivity) requireActivity()).refreshData();
                            } else {
                                MainActivity.say(getResources().getString(R.string.wrong_box));
                            }
                        } else {
                            if (App.state == App.SELECTGOODS) {
                                if (scannedCode.codeId.equals("c") && scannedCode.data.length() == 11) {
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
                                    currentGoods = Database.getGoodsRow(res);
                                    if (currentGoods == null) {
                                        MainActivity.say(getResources().getString(R.string.wrong_goods));
                                        FL.d(TAG, "Wrong goods code " + res);
                                    } else {
                                        int unitQnt = Database.getBarcodeQnt(res);
                                        ((MainActivity) requireActivity()).gotoGoodsFragment(currentGoods, unitQnt);
                                    }
                                } else {
                                    if (scannedCode.codeId.equals("b")) {
                                        FL.d(TAG, "Scan code39 =" + scannedCode.data);
                                        if (CheckCode.checkGoods39(scannedCode.data) && scannedCode.data.length() >= 11 && scannedCode.data.contains(".")) {
                                            String goodsId = scannedCode.data.substring(1, 10).replaceAll("\\.", " ");
                                            String goodsQnt = scannedCode.data.substring(10).replaceAll("\\.", "");
                                            int qnt = Integer.parseInt(goodsQnt, 36);
                                            FL.d(TAG, "Goods id ='" + goodsId + "' goods qnt ='" + goodsQnt + "' " + qnt);
                                            GoodsRow gr = Database.getGoodsRowById(goodsId);
                                            if (gr == null) {
                                                MainActivity.say(getResources().getString(R.string.wrong_goods));
                                                FL.d(TAG, "Wrong goods code " + scannedCode.data);
                                            } else {
                                                currentGoods = gr;
                                                ((MainActivity) requireActivity()).gotoGoodsFragment(currentGoods, qnt);
                                            }
                                        } else {
                                            currentGoods = Database.getGoodsRow(scannedCode.data);
                                            if (currentGoods == null) {
                                                MainActivity.say(getResources().getString(R.string.wrong_goods));
                                                FL.d(TAG, "Wrong goods code " + scannedCode.data);
                                            } else {
                                                int unitQnt = Database.getBarcodeQnt(scannedCode.data);
                                                ((MainActivity) requireActivity()).gotoGoodsFragment(currentGoods, unitQnt);
                                            }
                                        }
                                    } else {
                                        currentGoods = Database.getGoodsRow(scannedCode.data);
                                        if (currentGoods == null) {
                                            MainActivity.say(getResources().getString(R.string.wrong_goods));
                                            FL.d(TAG, "Wrong goods code " + scannedCode.data);
                                        } else {
                                            int unitQnt = Database.getBarcodeQnt(scannedCode.data);
                                            ((MainActivity) requireActivity()).gotoGoodsFragment(currentGoods, unitQnt);
                                        }
                                    }
                                }
                            } else {
                                MainActivity.say(getResources().getString(R.string.wrong_box));
                                FL.d(TAG, "Wrong box 3 scanned data " + scannedCode.data + " id " + scannedCode.codeId + " state " + App.state);
                            }
                        }
                    }
                }
            }
        }
    }
}