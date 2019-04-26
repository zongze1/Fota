package com.coagent.jac.s7.fota.dialog;

import com.abupdate.iov.Constant.EcuId;
import com.abupdate.iov.event.QueryEvent;
import com.abupdate.iov.event.info.EcuInfo;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.coagent.jac.s7.fota.R;

import java.util.List;

public class UpdateInfoAdapter extends BaseQuickAdapter<EcuInfo, BaseViewHolder> {
    public UpdateInfoAdapter(List<EcuInfo> data) {
        super(R.layout.rv_item_update_info, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, EcuInfo ecu) {
        EcuId.EcuEnum ecuEnum = EcuId.getEcuEnumById(ecu.ecuId);
        String ecuName = QueryEvent.getInstance().getECUName(ecuEnum);
        String srcVer = mContext.getString(R.string.update_info_src_version, ecu.srcVer);
        String dstVer = mContext.getString(R.string.update_info_dst_version, ecu.dstVer);
        helper.setText(R.id.rv_item_update_info_ecu_name, ecuName)
                .setText(R.id.rv_item_update_info_src_version, srcVer)
                .setText(R.id.rv_item_update_info_dst_version, dstVer);
    }
}
