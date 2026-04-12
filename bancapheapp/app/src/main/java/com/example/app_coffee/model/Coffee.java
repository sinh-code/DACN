package com.example.app_coffee.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "coffee_table")
public class Coffee implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private int price;
    private byte[] image; // Changed from int to byte[]
    private int quantity;


    public Coffee(String name, int price, byte[] image) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.quantity = 0;
    }

    protected Coffee(Parcel in) {
        id = in.readInt();
        name = in.readString();
        price = in.readInt();
        image = in.createByteArray();
        quantity = in.readInt();

    }

    public static final Creator<Coffee> CREATOR = new Creator<Coffee>() {
        @Override
        public Coffee createFromParcel(Parcel in) {
            return new Coffee(in);
        }

        @Override
        public Coffee[] newArray(int size) {
            return new Coffee[size];
        }
    };

    public Coffee(String caPheSua, int price, int coffeeSua) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(price);
        dest.writeByteArray(image);
        dest.writeInt(quantity);

    }

    // Getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
