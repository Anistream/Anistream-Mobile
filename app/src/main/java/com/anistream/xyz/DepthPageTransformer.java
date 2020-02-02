package com.anistream.xyz;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class DepthPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View page, float position ) {
        int pageWidth = page.getWidth();

        if ( position < -1 ) { // [ -Infinity,-1 )
            // This page is way off-screen to the left.
            page.setAlpha( 0 );

        } else if ( position <= 0 ) { // [-1,0]
            // Use the default slide transition when moving to the left page
            page.setAlpha( 1 );
            page.setTranslationX( 0 );
            page.setScaleX( 1 );
            page.setScaleY( 1 );

        } else if ( position <= 1 ) { // (0,1]
            // Fade the page out.
            page.setAlpha( 1 - position );

            // Counteract the default slide transition
            page.setTranslationX( pageWidth * -position );

            // Scale the page down ( between MIN_SCALE and 1 )
            float scaleFactor = MIN_SCALE
                    + ( 1 - MIN_SCALE ) * ( 1 - Math.abs( position ) );
            page.setScaleX( scaleFactor );
            page.setScaleY( scaleFactor );

        } else { // ( 1, +Infinity ]
            // This page is way off-screen to the right.
            page.setAlpha( 0 );
        }
    }
}