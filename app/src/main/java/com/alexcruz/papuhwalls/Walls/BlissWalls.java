package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class BlissWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_bliss_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_blisswalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "bliss_walls";
    }

}
