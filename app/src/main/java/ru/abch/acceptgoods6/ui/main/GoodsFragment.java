package ru.abch.acceptgoods6.ui.main;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bosphere.filelogger.FL;

import ru.abch.acceptgoods6.App;
import ru.abch.acceptgoods6.Cell;
import ru.abch.acceptgoods6.CheckCode;
import ru.abch.acceptgoods6.Config;
import ru.abch.acceptgoods6.Database;
import ru.abch.acceptgoods6.GoodsRow;
import ru.abch.acceptgoods6.MainActivity;
import ru.abch.acceptgoods6.R;
import ru.abch.acceptgoods6.ScannedCode;

public class GoodsFragment extends Fragment {
    private static final String TAG = "GoodsFragment";
    public static GoodsFragment newInstance(GoodsRow gr, int qnt) {
        GoodsFragment gf = new GoodsFragment();
        gf.goodsRow = gr;
        gf.qnt = qnt;
        gf.total = gr.qnt;
        FL.d(TAG,"Goods " + gr.article + " total " + gr.qnt + " qnt " + qnt);
        return gf;
    }
    EditText etCell, etQnt;
    TextView tvArticle, tvGoodsName, tvBrand;
    TextView tvRemain;
    GoodsRow goodsRow;
//    LiveData<ScannedCode> scannedData;
    int qnt, total;
    Cell cellIn;
    boolean cellSet = false, lostGoodsInput = false;
    ProgressBar pbWait;
    LoadGoodsTask loadGoodsTask;
//    String input;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.goods_fragment, container, false);
        tvArticle = view.findViewById(R.id.tv_article);
        tvGoodsName = view.findViewById(R.id.tv_goods);
        tvBrand = view.findViewById(R.id.tv_brand);
        etCell = view.findViewById(R.id.et_cell);
        etQnt = view.findViewById(R.id.et_qnt);
        tvRemain = view.findViewById(R.id.tv_remain);
        pbWait = view.findViewById(R.id.pb_wait);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvArticle.setText(goodsRow.article);
        tvGoodsName.setText(goodsRow.description);
        tvBrand.setText(goodsRow.brand);
        etCell.setText(goodsRow.cell);
        tvRemain.setText(String.valueOf(total));
        etCell.setOnFocusChangeListener((view, inFocus) -> {
            if(inFocus && !cellSet) {
                cellSet = true;
                etCell.setText("");
            }
        });
        etCell.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean inFocus) {
                if (inFocus) {
                    if (lostGoodsInput) {
                        Log.d(TAG, "Bypass cell input");
                        String sQnt = etQnt.getText().toString();
                        if (sQnt.length() > 0) {
                            qnt = Integer.parseInt(sQnt);
                        } else {
                            qnt = 0;
                        }
                        if (qnt > 0 && qnt <= total) {
                            AlertDialog.Builder adbConfirmLost = new AlertDialog.Builder(requireActivity());
                            adbConfirmLost.setTitle(R.string.lost_goods);
                            String msg = getResources().getString(R.string.goods_loss) + qnt;
                            adbConfirmLost.setMessage(msg);
                            adbConfirmLost.setNegativeButton(R.string.no, (dialogInterface, i) -> {
                                lostGoodsInput = false;
                                etQnt.setText("");
                                etQnt.requestFocus();
                            });
                            adbConfirmLost.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                                Log.d(TAG, "Confirm lost goods ");
                                FL.d(TAG, "Add lost goods " + goodsRow.article + " qnt " + qnt);
                                Database.addPlacedGoods(App.getStoreMan(), goodsRow.id, "",
                                        qnt, cellIn.id, MainActivity.getCurrentTime());
//                                ((MainActivity) requireActivity()).uploadGoods();
                                loadGoodsData();
                            });
                            adbConfirmLost.create().show();
                        } else {
                            MainActivity.say(getResources().getString(R.string.wrong_qnt));
                            etCell.setText("");
                            lostGoodsInput = false;
                            etQnt.setText("");
                            etQnt.setEnabled(true);
                            etQnt.requestFocus();
                        }
                    } else {
                        etCell.setText("");
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
                            result = App.storeCode[App.getStoreIndex()] + cellName + "000";
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
                                if(goodsRow.cellId == null || goodsRow.cellId.isEmpty() || cellIn.id.equals(goodsRow.cellId)) {
                                    etCell.setText(cellIn.descr);
                                    String sQnt = etQnt.getText().toString();
                                    if (sQnt.length() > 0) {
                                        qnt = Integer.parseInt(sQnt);
                                    } else {
                                        qnt = 0;
                                    }
                                    if (qnt > 0 && qnt <= total) {
                                        FL.d(TAG, "Add placed goods " + goodsRow.article + " qnt " + qnt);
                                        Database.addPlacedGoods(App.getStoreMan(), goodsRow.id, "",
                                                qnt, cellIn.id, MainActivity.getCurrentTime());
//                                        ((MainActivity) requireActivity()).uploadGoods();
                                        loadGoodsData();
                                        App.currentDistance = cellIn.distance;
                                    } else {
                                        MainActivity.say(getResources().getString(R.string.wrong_qnt));
                                        etCell.setText("");
                                        etQnt.setText("");
                                        etQnt.setEnabled(true);
                                        etQnt.requestFocus();
                                    }
                                } else {
                                    AlertDialog.Builder adbConfirmCell = new AlertDialog.Builder(requireActivity());
                                    adbConfirmCell.setTitle(R.string.change_cell);
                                    String msg = getResources().getString(R.string.confirm_cell_change);
                                    adbConfirmCell.setMessage(msg);
                                    adbConfirmCell.setNegativeButton(R.string.no, (dialogInterface, i) -> {
                                        lostGoodsInput = false;
                                        etCell.setText("");
                                        etCell.setEnabled(true);
                                        etCell.requestFocus();
                                    });
                                    adbConfirmCell.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                                        Log.d(TAG, "Confirm cell change");
                                        etCell.setText(cellIn.descr);
                                        String sQnt = etQnt.getText().toString();
                                        if (sQnt.length() > 0) {
                                            qnt = Integer.parseInt(sQnt);
                                        } else {
                                            qnt = 0;
                                        }
                                        if (qnt > 0 && qnt <= total) {
                                            FL.d(TAG, "Add placed goods " + goodsRow.article + " qnt " + qnt);
                                            Database.addPlacedGoods(App.getStoreMan(), goodsRow.id, "",
                                                    qnt, cellIn.id, MainActivity.getCurrentTime());
//                                            ((MainActivity) requireActivity()).uploadGoods();
                                            loadGoodsData();
                                        } else {
                                            MainActivity.say(getResources().getString(R.string.wrong_qnt));
                                            etCell.setText("");
                                            etQnt.setText("");
                                            etQnt.setEnabled(true);
                                            etQnt.requestFocus();
                                        }
                                    });
                                    adbConfirmCell.create().show();
                                }
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
        if(qnt > 0) {
            etQnt.setText(String.valueOf(qnt));
            etQnt.setSelection(etQnt.getText().length());
        } else {
            etQnt.setText("");
        }
        etQnt.requestFocus();
    }

    private void addScannedGoods(String barcode) {
        GoodsRow scannedGoods = Database.getGoodsRow(barcode);
        if (scannedGoods != null) {
            if (scannedGoods.id.equals(goodsRow.id)) {
                int unitQnt = Database.getBarcodeQnt(barcode);
                if(unitQnt + qnt <= total) {
                    qnt += unitQnt;
                    etQnt.setText(String.valueOf(qnt));
                    etQnt.setSelection(etQnt.getText().length());
                    FL.d(TAG, "Barcode " + barcode +" goodsId ='" + scannedGoods.id + "' unit qnt " + unitQnt +
                            " qnt " + qnt + " article " + scannedGoods.article + " total " + total);
                } else {
                    FL.d(TAG,"Excessive goods " + (qnt +unitQnt));
                    MainActivity.say(getResources().getString(R.string.wrong_qnt));
                }
            } else {
                MainActivity.say(getResources().getString(R.string.wrong_goods));
                FL.d(TAG, "Differ goods code");
            }
        } else {
            MainActivity.say(getResources().getString(R.string.wrong_goods));
            FL.d(TAG, "Goods code " + barcode + " not found");
        }
    }
    public void processLost(Cell lostGoodsCell) {
        etCell.setText(lostGoodsCell.descr);
        cellIn = lostGoodsCell;
        lostGoodsInput = true;
        String sQnt = etQnt.getText().toString();
        if (sQnt.length() > 0) {
            qnt = Integer.parseInt(sQnt);
        } else {
            qnt = 0;
        }
        if (qnt > 0 && qnt <= total) {
            FL.d(TAG, "Add lost goods " + goodsRow.article + " qnt " + qnt);
            Database.addPlacedGoods(App.getStoreMan(), goodsRow.id, "",
                    qnt, cellIn.id, MainActivity.getCurrentTime());
//            ((MainActivity) requireActivity()).uploadGoods();
            loadGoodsData();
            App.currentDistance = cellIn.distance;
        } else {
            MainActivity.say(getResources().getString(R.string.wrong_qnt));
            etCell.setText("");
            etQnt.setText("");
            etQnt.setEnabled(true);
            etQnt.requestFocus();
        }
    }
    class LoadGoodsTask extends AsyncTask<Void, Void, Void>  {
        String TAG = "LoadGoodsTask";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute");
            requireActivity().runOnUiThread(showProgress);
        }

        @Override
        protected Void doInBackground(Void... params) {
            MainActivity.mViewModel.loadGoodsData();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute");
//            requireActivity().runOnUiThread(finishFragment);
            App.state = App.SELECTGOODS;
            FL.d(TAG, "gotoMainFragment() state " + App.state);
            ((MainActivity) requireActivity()).gotoMainFragment();
        }
    }
    private void loadGoodsData() {
        loadGoodsTask = new LoadGoodsTask();
        loadGoodsTask.execute();
    }
    Runnable showProgress = new Runnable() {
        @Override
        public void run() {
            pbWait.setVisibility(View.VISIBLE);
        }
    };
    /*
    Runnable finishFragment = new Runnable() {
        @Override
        public void run() {
//            pbWait.setVisibility(View.GONE);
            App.state = App.SELECTGOODS;
            FL.d(TAG, "gotoMainFragment() state " + App.state);
            ((MainActivity) requireActivity()).gotoMainFragment();
        }
    };

     */
    @Override
    public void onStop() {
        if (this.loadGoodsTask != null && this.loadGoodsTask.getStatus() == AsyncTask.Status.RUNNING) this.loadGoodsTask.cancel(true);
        super.onStop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.loadGoodsTask != null && this.loadGoodsTask.getStatus() == AsyncTask.Status.RUNNING) this.loadGoodsTask.cancel(true);
    }
    public void processScan(ScannedCode scannedCode) {
        if (scannedCode != null) {
            if (App.state == App.PUTGOODS) {
                if (scannedCode.codeId.equals("b")) {
                    FL.d(TAG, "Scan code39 =" + scannedCode.data);
                    if (CheckCode.checkGoods39(scannedCode.data) && scannedCode.data.length() >= 11 && scannedCode.data.contains(".")) {
                        String goodsId = scannedCode.data.substring(1, 10).replaceAll("\\.", " ");
                        String goodsQnt = scannedCode.data.substring(10).replaceAll("\\.", "");
                        int unitQnt = Integer.parseInt(goodsQnt, 36);
                        FL.d(TAG, "Goods id ='" + goodsId + "' goods qnt ='" + goodsQnt + "' " + qnt + " article " + goodsRow.article + " total " + total);
                        if(goodsId.equals(goodsRow.id)) {
                            if(unitQnt + qnt <= total) {
                                qnt += unitQnt;
                                etQnt.setText(String.valueOf(qnt));
                                etQnt.setSelection(etQnt.getText().length());
                            } else {
                                FL.d(TAG,"Excessive goods " + (qnt +unitQnt));
                                MainActivity.say(getResources().getString(R.string.wrong_qnt));
                                etQnt.setEnabled(true);
                                etQnt.requestFocus();
                            }
                        } else {
                            MainActivity.say(getResources().getString(R.string.wrong_goods));
                            FL.d(TAG, "Wrong goods code");
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
//                    cellIn = res.startsWith(App.storeCode[App.getStoreIndex()]) || res.startsWith("1900") && App.getStoreId().equals("    12SPR")? Database.getCellByName(Config.getCellName(res)) : null;
                    String code = App.codeMap.get(App.getStoreId()) == null? "aa" : App.codeMap.get(App.getStoreId());
                    assert code != null;
                    cellIn = res.startsWith(code) || res.startsWith("1900") && App.getStoreId().equals("    12SPR")?
                            Database.getCellByName(Config.getCellName(res)) : null;
                    if (cellIn != null) {
                        FL.d(TAG, "Scanned cell " + cellIn.descr);
                        if(goodsRow.cellId == null || goodsRow.cellId.isEmpty() || cellIn.id.equals(goodsRow.cellId)) {
                            String sQnt = etQnt.getText().toString();
                            if (sQnt.length() > 0) {
                                qnt = Integer.parseInt(sQnt);
                            } else {
                                qnt = 0;
                            }
                            if (qnt > 0 && qnt <= total) {
                                FL.d(TAG, "Add placed goods " + goodsRow.article + " qnt " + qnt);
                                Database.addPlacedGoods(App.getStoreMan(), goodsRow.id, "",
                                        qnt, cellIn.id, MainActivity.getCurrentTime());
//                                ((MainActivity) requireActivity()).uploadGoods();
                                loadGoodsData();
                                App.currentDistance = cellIn.distance;
                            } else {
                                MainActivity.say(getResources().getString(R.string.wrong_qnt));
                                etCell.setText("");
                                etQnt.setText("");
                                etQnt.setEnabled(true);
                                etQnt.requestFocus();
                            }
                        }  else {
                            AlertDialog.Builder adbConfirmCell = new AlertDialog.Builder(requireActivity());
                            adbConfirmCell.setTitle(R.string.change_cell);
                            String msg = getResources().getString(R.string.confirm_cell_change);
                            adbConfirmCell.setMessage(msg);
                            etCell.setText(cellIn.descr);
                            adbConfirmCell.setNegativeButton(R.string.no, (dialogInterface, i) -> {
                                lostGoodsInput = false;
                                etCell.setText("");
                                etCell.requestFocus();
                            });
                            adbConfirmCell.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                                Log.d(TAG, "Confirm cell change");
                                String sQnt = etQnt.getText().toString();
                                if (sQnt.length() > 0) {
                                    qnt = Integer.parseInt(sQnt);
                                } else {
                                    qnt = 0;
                                }
                                if (qnt > 0 && qnt <= total) {
                                    FL.d(TAG, "Add placed goods " + goodsRow.article + " qnt " + qnt);
                                    Database.addPlacedGoods(App.getStoreMan(), goodsRow.id, "",
                                            qnt, cellIn.id, MainActivity.getCurrentTime());
//                                    ((MainActivity) requireActivity()).uploadGoods();
                                    loadGoodsData();
                                } else {
                                    MainActivity.say(getResources().getString(R.string.wrong_qnt));
                                    etCell.setText("");
                                    etQnt.setText("");
                                    etQnt.setEnabled(true);
                                    etQnt.requestFocus();
                                }
                            });
                            adbConfirmCell.create().show();
                        }
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
    }
}