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

        int screenWidth = canvas.getWidth();
        int cellWidth = screenWidth/boardSize;

        for(int i = 0; i < boardSize*boardSize; i++){
            int row = i/boardSize;
            int column = i%boardSize;

            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            if(!getMap(row,column).processing){
                float dv = ((float) (getMap(row,column).height))/MAX_HEIGHT;
                paint.setColor(Color.rgb((int)(dv*127)+128,(int)(dv*127)+128,(int)(dv*127)+128));
            }else if(getMap(row,column).flowsNW && !getMap(row,column).flowsSE){
                paint.setColor(Color.GREEN);
            } else if(getMap(row,column).flowsSE && !getMap(row,column).flowsNW){
                paint.setColor(Color.BLUE);
            }else if(getMap(row,column).flowsNW && getMap(row,column).flowsSE){
                paint.setColor(Color.RED);
            }else if(!getMap(row,column).flowsNW && !getMap(row,column).flowsSE){
                float dv = ((float) (getMap(row,column).height))/MAX_HEIGHT;
                paint.setColor(Color.rgb((int)(dv*127)+128,(int)(dv*127)+128,(int)(dv*127)+128));

            }

            canvas.drawRect(row*cellWidth,column*cellWidth,row*cellWidth + cellWidth, column*cellWidth + cellWidth,paint);

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(50*5/boardSize);
            canvas.drawText(Integer.toString(getMap(row,column).height),row*cellWidth + 2*cellWidth/3,column*cellWidth + 2*cellWidth/3,paint);

        }

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

        Cell cell = getMap(x,y);
        cell.flowsNW = flowsNW || cell.flowsNW;;
        cell.flowsSE = flowsSE || cell.flowsSE;
        cell.basin = !flowsNW && !flowsSE;
        cell.processing = true;

        if(isMaxHeight(x,y)){

            return;

        }else {

            if(isValid(x+1,y) && getMap(x+1,y).height > getMap(x, y).height )// &&height of (x+1,y) > height of x,y)//
            {
                buildUpContinentalDivideRecursively(x+1,y,flowsNW,flowsSE,cell.height);
            }
            if(isValid(x,y+1)&& getMap(x,y+1).height > getMap(x, y).height){
                buildUpContinentalDivideRecursively(x,y+1,flowsNW,flowsSE,cell.height);
            }
            if(isValid(x-1,y)&& getMap(x-1,y).height > getMap(x, y).height){
                buildUpContinentalDivideRecursively(x-1,y,flowsNW,flowsSE,cell.height);
            }
            if(isValid(x,y-1)&& getMap(x,y-1).height > getMap(x, y).height){
                buildUpContinentalDivideRecursively(x,y-1,flowsNW,flowsSE,cell.height);
            }

        }

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
        if(isValid(x+1,y) && getMap(x+1,y).height > getMap(x,y).height){
            return false;
        }else if(isValid(x-1,y) && getMap(x-1,y).height > getMap(x,y).height){
            return false;
        } else if(isValid(x,y+1) && getMap(x,y+1).height > getMap(x,y).height){
            return false;
        }else if(isValid(x,y-1) && getMap(x,y-1).height > getMap(x,y).height){
            return false;
        }

        return true;

    }

    public void buildDownContinentalDivide(boolean oneStep) {
        if (!oneStep)
            clearContinentalDivide();
        while (true) {
            int maxUnprocessedX = -1, maxUnprocessedY = -1, foundMaxHeight = -1;
            for (int y = 0; y < boardSize; y++) {
                for (int x = 0; x < boardSize; x++) {
                    Cell cell = getMap(x, y);
                    if (!cell.processing && cell.height > foundMaxHeight) {
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

        if(getMap(x,y).processing){

            return getMap(x,y);

        } else{

            if(x == 0 || y == 0){
                getMap(x,y).flowsNW = true;
            }

            if(x == boardSize-1 || y == boardSize - 1){
                getMap(x,y).flowsSE = true;
            }

            Cell workingCell = new Cell();

            if(isValid(x+1,y) && getMap(x+1,y).height < getMap(x, y).height )
            {
                workingCell = buildDownContinentalDivideRecursively(x+1,y,getMap(x, y).height);
                getMap(x,y).flowsNW = getMap(x,y).flowsNW || workingCell.flowsNW;
                getMap(x,y).flowsSE = getMap(x,y).flowsSE || workingCell.flowsSE;

            }

            if(isValid(x,y+1)&& getMap(x,y+1).height < getMap(x, y).height){

                workingCell = buildDownContinentalDivideRecursively(x,y+1,getMap(x, y).height);
                getMap(x,y).flowsNW = getMap(x,y).flowsNW || workingCell.flowsNW;
                getMap(x,y).flowsSE = getMap(x,y).flowsSE || workingCell.flowsSE;

            }

            if(isValid(x-1,y)&& getMap(x-1,y).height < getMap(x, y).height){

                workingCell = buildDownContinentalDivideRecursively(x-1,y,getMap(x, y).height);
                getMap(x,y).flowsNW = getMap(x,y).flowsNW || workingCell.flowsNW;
                getMap(x,y).flowsSE = getMap(x,y).flowsSE || workingCell.flowsSE;


            }

            if(isValid(x,y-1)&& getMap(x,y-1).height < getMap(x, y).height){


                workingCell = buildDownContinentalDivideRecursively(x,y-1,getMap(x, y).height);
                getMap(x,y).flowsNW = getMap(x,y).flowsNW || workingCell.flowsNW;
                getMap(x,y).flowsSE = getMap(x,y).flowsSE || workingCell.flowsSE;

            }

            getMap(x,y).processing = true;

            return getMap(x,y);

        }

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

        //set the four corners to some pre-defined value
        //Random random = new Random();

        map[0].height = random.nextInt(255);
        map[boardSize - 1].height = random.nextInt(255);
        map[boardSize*boardSize -1].height = random.nextInt(255);
        map[boardSize*boardSize - boardSize].height = random.nextInt(255);

        /*int middleElementX = ((boardSize*boardSize - 1)/boardSize + (boardSize -1)/boardSize)/2;
        int middleElementY = (0%boardSize + (boardSize -1)%boardSize)/2;

        int middleElement = middleElementX*boardSize + middleElementY;
        int topMiddleElement = (boardSize - 1 + 0)/2;
        int bottomMiddleElement = ((boardSize*boardSize -1) + (boardSize*boardSize - boardSize))/2;
        int rightMiddleElement = middleElement + (((boardSize -1) -(0)))/2;
        int leftMiddleElement = middleElement - ((boardSize -1) - (0))/2;

        double middleAverage = ((double) (map[0].height + map[boardSize - 1].height + map[boardSize*boardSize -1].height + map[boardSize*boardSize - boardSize].height))/4;

        map[middleElement].height = (int)middleAverage + random.nextInt(10);
        map[topMiddleElement].height = (int)((map[boardSize - 1].height+map[0].height)/2 + random.nextInt(8));

        map[bottomMiddleElement].height = (int)((map[boardSize*boardSize -1].height+map[boardSize*boardSize - boardSize].height)/2 + random.nextInt(6));

        map[rightMiddleElement].height = (int)((map[boardSize - 1].height+map[boardSize*boardSize -1].height)/2 + random.nextInt(5));

        map[leftMiddleElement].height = (int)((map[0].height+map[boardSize*boardSize - boardSize].height)/2 + random.nextInt(3));*/


        diamondSquare(map,0,boardSize -1,boardSize*boardSize - boardSize, boardSize*boardSize - 1);
        //create a function that takes the the map array and pointers to the four corners of the array

        /**
         **
         **  YOUR CODE GOES HERE
         **
         **/
        invalidate();
    }

    private void diamondSquare(Cell[] map, int topLeft, int topRight, int bottomLeft, int bottomRight) {



        if(squareSizeTooSmall(topLeft,topRight, bottomLeft,bottomRight)){
            return;
        }

        double middleAverage = ((double) (map[topLeft].height + map[topRight].height + map[bottomLeft].height + map[bottomRight].height))/4;



        int middleElementX = (bottomRight/boardSize + topRight/boardSize)/2;
        int middleElementY = (topLeft%boardSize + topRight%boardSize)/2;

        //getMap(middleElementX,middleElementY).height = (int)((double) (map[topLeft].height + map[topRight].height + map[bottomLeft].height + map[bottomRight].height))/4 + random.nextInt(10);

        int middleElement = middleElementX*boardSize + middleElementY;

        int topMiddleElement = (topRight + topLeft)/2;
        int bottomMiddleElement = (bottomRight + bottomLeft)/2;
        int rightMiddleElement = middleElement + ((topRight - topLeft))/2;
        int leftMiddleElement = middleElement - ((topRight - topLeft))/2;


        map[middleElement].height = (int)middleAverage + random.nextInt(10);
        map[topMiddleElement].height = (int)((map[topRight].height+map[topLeft].height)/2 + random.nextInt(8));
        map[bottomMiddleElement].height = (int)((map[bottomRight].height+map[bottomLeft].height)/2 + random.nextInt(6));
        map[rightMiddleElement].height = (int)((map[topRight].height+map[bottomRight].height)/2 + random.nextInt(5));
        map[leftMiddleElement].height = (int)((map[topLeft].height+map[bottomLeft].height)/2 + random.nextInt(3));

        diamondSquare(map,topLeft,topMiddleElement,middleElement,leftMiddleElement);
        diamondSquare(map,middleElement,rightMiddleElement,bottomRight,bottomMiddleElement);
        diamondSquare(map,topMiddleElement,topRight,rightMiddleElement,middleElement);
        diamondSquare(map,leftMiddleElement,middleElement,bottomMiddleElement,bottomLeft);

    }

    private boolean squareSizeTooSmall(int topLeft, int topRight, int bottomLeft, int bottomRight) {
        return (topRight - topLeft+1)*((bottomRight/boardSize - topLeft/boardSize) + 1) < 9;

    }

}
