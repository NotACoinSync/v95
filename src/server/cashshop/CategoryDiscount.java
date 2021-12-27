package server.cashshop;

import java.sql.ResultSet;
import java.sql.SQLException;

import tools.data.output.LittleEndianWriter;

public class CategoryDiscount {

    public byte aCategory, nCategorySub, nDiscountRate;

    public void encode(LittleEndianWriter lew) {// CShopInfo::EncodeCategoryDicountRate
        lew.write(aCategory);
        lew.write(nCategorySub);
        lew.write(nDiscountRate);
    }

    public void load(ResultSet rs) throws SQLException {
        aCategory = rs.getByte("category");
        nCategorySub = rs.getByte("categorySub");
        nDiscountRate = rs.getByte("discountRate");
    }
}
