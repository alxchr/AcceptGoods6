package ru.abch.acceptgoods6.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import java.util.ArrayList;

import ru.abch.acceptgoods6.App;
import ru.abch.acceptgoods6.Cell;
import ru.abch.acceptgoods6.Database;
import ru.abch.acceptgoods6.GoodsRow;
import ru.abch.acceptgoods6.MainActivity;
import ru.abch.acceptgoods6.ScannedCode;

public class MainViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private final String TAG = "MainViewModel";
    MutableLiveData<ArrayList<GoodsRow>> goodsData;
    MutableLiveData<Cell> cell;
    MutableLiveData<GoodsRow> currentGoods;
    MutableLiveData<ScannedCode> scannedMainFragment, scannedGoodsFragment, scannedEGFragment;
    /*
    public void loadScannedData(ScannedCode sc, int state) {
        if(state == App.SELECTEXCESSIVEGOODS) {
            scannedEGFragment.postValue(sc);
//            Log.d(TAG,"To ExcessiveGoodsFragment data " + sc.data + " id " + sc.codeId);
        } else if(state == App.PUTGOODS) {
            scannedGoodsFragment.postValue(sc);
//            Log.d(TAG,"To GoodsFragment data " + sc.data + " id " + sc.codeId);
        } else {
            scannedMainFragment.postValue(sc);
//            Log.d(TAG,"To MainFragment data " + sc.data + " id " + sc.codeId);
        }
    }

    public LiveData<ScannedCode> getScannedEGFragment() {
        if(scannedEGFragment == null) {
            scannedEGFragment = new MutableLiveData<>();
            scannedEGFragment.postValue(null);
        }
        return scannedEGFragment;
    }
    public LiveData<ScannedCode> getScannedGoodsFragment() {
        if(scannedGoodsFragment == null) {
            scannedGoodsFragment = new MutableLiveData<>();
            scannedGoodsFragment.postValue(null);
        }
        return scannedGoodsFragment;
    }
    public LiveData<ScannedCode> getScannedMainFragment() {
        if(scannedMainFragment == null) {
            scannedMainFragment = new MutableLiveData<>();
            scannedMainFragment.postValue(null);
        }
        return scannedMainFragment;
    }

     */
    public LiveData<ArrayList<GoodsRow>> getGoodsData() {
        if (goodsData == null) {
            Log.d(TAG, "Init data");
            goodsData = new MutableLiveData<>();
            loadGoodsData();
        }
        return goodsData;
    }
    public void loadGoodsData() {
        ArrayList<GoodsRow> taskList = (App.getPackMode())? Database.getPackList() : Database.getTaskList();
        taskList.sort((lhs, rhs) -> {
            int ret = 0;
            if (lhs != null && rhs != null) {
                int leftDistance = lhs.getDistance(App.currentDistance);
                int rightDistance = rhs.getDistance(App.currentDistance);
                ret = Integer.compare(leftDistance, rightDistance);
            }
            return ret;
        });
        Log.d(TAG, "Task list size " + taskList.size());
        if(goodsData != null) goodsData.postValue(taskList);
    }

    public LiveData<GoodsRow> getCurrentGoods() {
        if(currentGoods == null) {
            Log.d(TAG, "Init current goods");
            currentGoods = new MutableLiveData<>();
        }
        return currentGoods;
    }
    public void loadCurrentGoods(GoodsRow gr) {
        currentGoods.postValue(gr);
    }

    public LiveData<Cell> getDestinationCell() {
        if (cell == null) {
            cell = new MutableLiveData<>();
            loadDestinationCell();
        }
        return cell;
    }
    private void loadDestinationCell() {
        cell.postValue(null);
    }
    public void loadDestinationCell(Cell c) {
        cell.postValue(c);
    }
}