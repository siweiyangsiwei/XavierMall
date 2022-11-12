package com.xavier.mall.ware.vo;

import cn.hutool.poi.excel.style.Align;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseDoneVo {
    @NotNull
    private Long id;
    private List<PurchaseItemDoneVo> items;
}
