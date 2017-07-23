/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.continentaldivide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;


public class ContinentMap extends View {
    public static final int MAX_HEIGHT = 255;
    private Cell[] map;
    private int boardSize;
    private Random random = new Random();
    private int maxHeight = 0, minHeight = 0;

    private Integer[] DEFAULT_MAP = {
            1, 2, 3, 4, 5,
            2, 3, 4, 5, 6,
            3, 4, 5, 3, 1,
            6, 7, 3, 4, 5,
            5, 1, 2, 3, 4,
    };

    private int[] MAP_COLORS = new int[DEFAULT_MAP.length];

    public ContinentMap(Context context) {
        super(context);

        boardSize = (int) (Math.sqrt(DEFAULT_MAP.length));
        map = new Cell[boardSize * boardSize];
        for (int i = 0; i < boardSize * boardSize; i++) {
            map[i] = new Cell();
            map[i].height = DEFAULT_MAP[i];
        }
        maxHeight = Collections.max(Arrays.asList(DEFAULT_MAP));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }

    private class Cell {
        protected int height = 0;
        protected boolean flowsNW = false;
        protected boolean flowsSE = false;
        protected boolean basin = false;
        protected boolean processing = false;
    }

    private Cell getMap(int x, int y) {
        if (x >=0 && x < boardSize && y >= 0 && y < boardSize)
            return map[x + boardSize * y];
        else
            return null;
    }

    public void clearContinentalDivide() {
        for (int i = 0; i < boardSize * boardSize; i++) {
            map[i].flowsNW = false;
            map[i].flowsSE = false;
            map[i].basin = false;
            map[i].processing = false;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();

        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        int screenHeight = canvas.getHeight();
        int screenWidth = canvas.getWidth();
        int cellWidth = screenWidth/boardSize;



        for(int i = 0; i < boardSize*boardSize; i++){
            int row = i/boardSize;
            int column = i%boardSize;

            //paint.setARGB(0,DEFAULT_MAP[i]*(maxHeight/7),DEFAULT_MAP[i]*(maxHeight/7),DEFAULT_MAP[i]*(maxHeight/7));
            //paint.setColor(Color.rgb(DEFAULT_MAP[i]*(maxHeight/7),DEFAULT_MAP[i]*(maxHeight/7),DEFAULT_MAP[i]*(maxHeight/7)));

            float dv = (DEFAULT_MAP[i].floatValue())/7;
            paint.setColor(Color.rgb((int)(dv*127)+128,(int)(dv*127)+128,(int)(dv*127)+128));
            //paint.setColor(Color.RED);
            canvas.drawRect(row*cellWidth,column*cellWidth,row*cellWidth + cellWidth, column*cellWidth + cellWidth,paint);

            //given x = row and y = column
            //to convert to coordinate it is simply x*width and y*height, width, height, paint

        }
        /**
         **
         **  YOUR CODE GOES HERE
         * using the default map and a for loop, draw a rectangle of uniform width and height, at different locations with the color of the rectangles
         * depending on the value in default map.
         **
         **/
    }

    public void buildUpContinentalDivide(boolean oneStep) {
        if (!oneStep)
            clearContinentalDivide();
        boolean iterated = false;
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                Cell cell = getMap(x, y);
                if ((x == 0 || y == 0 || x == boardSize - 1 || y == boardSize - 1)) {
                    buildUpContinentalDivideRecursively(
                            x, y, x == 0 || y == 0, x == boardSize - 1 || y == boardSize - 1, -1);
                    if (oneStep) {
                        iterated = true;
                        break;
                    }
                }
            }
            if (iterated && oneStep)
                break;
        }
        invalidate();
    }

    private void buildUpContinentalDivideRecursively(
            int x, int y, boolean flowsNW, boolean flowsSE, int previousHeight) {

        Cell cell = new Cell();
        if(isMaxHeight(x,y)){

            //if current cell has been proccessed, then check if it's current flow color combination Union the new one makes it a continental
            //divide. in other words, if it was (true, false) in the first one and (false true)
            //boardSize*x + y
            MAP_COLORS[boardSize*x + y] = Color.RED;

            cell.flowsNW = flowsNW || cell.flowsNW;;
            cell.flowsSE = flowsSE || cell.flowsSE;
            cell.processing = true;
            map[boardSize*x+y] = cell;
            return;

            //if none of the neighbors are valid then return
            //return


            //get the color of this specific thing, color it that way
        }else {

            //if current cell has been proccessed, then check if it's current flow color combination Union the new one makes it a continental
            //divide. in other words, if it was (true, false) in the first one and (false true)
            cell.flowsNW = flowsNW || cell.flowsNW;
            cell.flowsSE = flowsSE || cell.flowsSE;
            cell.basin = !flowsNW && !flowsSE;
            cell.processing = true;
            map[boardSize*x + y] = cell;


            if(isValid(x+1,y) )// &&height of (x+1,y) > height of x,y)//
            {
                buildUpContinentalDivideRecursively(x+1,y,flowsNW,flowsSE,cell.height);

            }
            if(isValid(x,y+1)){
                buildUpContinentalDivideRecursively(x,y+1,flowsNW,flowsSE,cell.height);

            }
            if(isValid(x-1,y)){
                buildUpContinentalDivideRecursively(x-1,y,flowsNW,flowsSE,cell.height);

            }
            if(isValid(x,y-1)){
                buildUpContinentalDivideRecursively(x,y-1,flowsNW,flowsSE,cell.height);

            }

            //color the cell at x and y to the corresponding color of whichever is true of flowsNW or flowsSE
            //check each one of the neighbors for validity as far as going off the board goes and increasing height
            // then check if the proccesign flag is true for the current cell and if so check if both SW and NE are true. if so,
            //then it is a continental divide
            //if the above condition evaluates to true:
            //recursively call on the neighbouring cell by passing the right flow and also pass the current height as previousHeight

            /*
            What is unneccessarily being repeated here:
            1. After base case is reached, a cell may be recomputed even if the color has already been computed instead of returning


             */
        }
        /**
         **
         **  YOUR CODE GOES HERE
         * base case is when you reach a continental divide which is a point where nothing flows or it flows to both. when you reach that point, return
         * otherwise, color the current cell with the same color as the one that lead up to it. recursively do this for each neighboring cell that is
         * at a higher altitude and happens to be valid(in the board)
         *
         **
         **/
    }

    //checks if the current cell is a valid cell
    private boolean isValid(int x, int y) {
        //
        if(x > boardSize -1 || x < 0 || y > boardSize -1 || y < 0){
            return false;
        }
        return true;

    }

    private boolean isMaxHeight(int x, int y) {

        //check if x+1 is a valid cell and if so a higher hight. if so return false
        //else if check y+1 is a valid cell and if it is a heigher point. if so return false
        //...

        return false;

    }

    public void buildDownContinentalDivide(boolean oneStep) {
        if (!oneStep)
            clearContinentalDivide();
        while (true) {
            int maxUnprocessedX = -1, maxUnprocessedY = -1, foundMaxHeight = -1;
            for (int y = 0; y < boardSize; y++) {
                for (int x = 0; x < boardSize; x++) {
                    Cell cell = getMap(x, y);
                    if (!(cell.flowsNW || cell.flowsSE || cell.basin) && cell.height > foundMaxHeight) {
                        maxUnprocessedX = x;
                        maxUnprocessedY = y;
                        foundMaxHeight = cell.height;
                    }
                }
            }
            if (maxUnprocessedX != -1) {
                buildDownContinentalDivideRecursively(maxUnprocessedX, maxUnprocessedY, foundMaxHeight + 1);
                if (oneStep) {
                    break;
                }
            } else {
                break;
            }
        }
        invalidate();
    }

    private Cell buildDownContinentalDivideRecursively(int x, int y, int previousHeight) {
        Cell workingCell = new Cell();
        /**
         * base case is if you reach the coast at which point you return the cell whose color you already know
         * otherwise:
         * recursively call the method with the coordinates of each cell that is valid(in the board and decending height) and the current height and do
         * the following for each of thoes recursive calls:
         * get the recursively returned cell value from caling the function in the above step
         * assign its color to the current cell
         *
         **
         **
         **  YOUR CODE GOES HERE
         **
         **/
        return workingCell;
    }

    public void generateTerrain(int detail) {
        int newBoardSize = (int) (Math.pow(2, detail) + 1);
        if (newBoardSize != boardSize * boardSize) {
            boardSize = newBoardSize;
            map = new Cell[boardSize * boardSize];
            for (int i = 0; i < boardSize * boardSize; i++) {
                map[i] = new Cell();
            }
        }
        /**
         **
         **  YOUR CODE GOES HERE
         **
         **/
        invalidate();
    }
}
