package com.coagent.jac.s7.fota;

import com.abupdate.iov.event.info.EcuInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateInfo {
    private boolean schedule = false;
    private int upgradeTime;
    private List<EcuInfo> ecuInfoList;

    public void release() {
        ecuInfoList.clear();
        ecuInfoList = null;
    }

    public int getUpgradeTime() {
        return upgradeTime;
    }

    public void setUpgradeTime(int upgradeTime) {
        this.upgradeTime = upgradeTime;
    }

    public List<EcuInfo> getEcuInfoList() {
        return ecuInfoList;
    }

    public void setEcuInfoList(EcuInfo[] array) {
        this.ecuInfoList = new ArrayList<>();
        ecuInfoList.addAll(Arrays.asList(array));
    }

    public boolean isSchedule() {
        return schedule;
    }

    public void setSchedule(boolean schedule) {
        this.schedule = schedule;
    }
}
