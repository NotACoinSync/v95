/* 
 * This file is part of the Elation Maple Story Server
 *
 * Copyright (C) 2019 Tim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* Heart MaxHP & MaxMP Exchange
 * Credits Amity & Whiskey
 */

var status;
function start() {
status = -1;//sets status to -1
action( 1, 0, 0);
}
function action (mode, type , selection) {
if (mode == 1) { 
     status++; 
 }else{ 
       status--; 
}
if (status == 0) { 
cm.sendSimple("I am willing to increase for MaxHP for #v4310008# or MaxMP for #v4310009#. You can gain them from bosses drops. Which one do you want to exchange? #d\r\n#L0# #v4310008# #b\r\n#L1# #v4310009# #l");
}else if (status == 1){
if (selection == 0) {
cm.sendSimple("#eAre you sure you want to increase your MaxHP? #r\r\n#L100# Yes");
}else if (selection== 1){
cm.sendSimple("#eAre you sure you want to increase your MaxMP? #r\r\n#L102# Yes");
}else if (selection == 2){
cm.sendSimple("#b\r\n\#L118# Reverse Executioners #b\r\n\#L119# Reverse Bardiche #b\r\n\#L120# Reverse Allergando #b\r\n\#L121# Reverse Pescas #b\r\n\#L122# Reverse Killic #b\r\n\#L123# Reverse EnrealTear #b\r\n\#L124# Reverse Aeas Hand #b\r\n\#L125# Reverse Neibelheim #b\r\n\#L126# Reverse Tabarzin #b\r\n\#L127# Reverse Bellocce #b\r\n\#L128# Reverse Alchupiz #b\r\n\#L129# Reverse Diesra #b\r\n\#L130# Reverse Engaw #b\r\n\#L131# Reverse Black Beauty #b\r\n\#L132# Reverse Lampion #b\r\n\#L133# Reverse Equinox #b\r\n\#L134# Reverse Blindness");
     } 
}else if (status == 2){
if (selection == 100){
if (cm.getItem(4310008)>= 1){
cm.gainMaxHP(100);
cm.gainItem(4310008, -1);
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection == 101){
if (cm.getvotePoints() >= 6){
cm.gainItem(2340000, 5);
cm.gainvotePoints(-15);
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection == 102){
if (cm.getvotePoints() >= 5){
cm.gainItem(1302059, 1);
cm.gainvotePoints(-5);
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 103){
if (cm.getvotePoints() >= 5){
cm.gainItem(1312031, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 104){
if (cm.getvotePoints() >= 5){
cm.gainItem(1322052, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 105){
if (cm.getvotePoints() >= 5){
cm.gainItem(1372032, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 106){
if (cm.getvotePoints() >= 5){
cm.gainItem(1382036, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 107){
if (cm.getvotePoints() >= 5){
cm.gainItem(1332049, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 108){
if (cm.getvotePoints() >= 5){
cm.gainItem(1402036, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 109){
if (cm.getvotePoints() >= 5){
cm.gainItem(1412026, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 110){
if (cm.getvotePoints() >= 5){
cm.gainItem(1422028, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 111){
if (cm.getvotePoints() >= 5){
cm.gainItem(1432038, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 112){
if (cm.getvotePoints() >= 5){
cm.gainItem(1442045, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 113){
if (cm.getvotePoints() >= 5){
cm.gainItem(1452044, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 114){
if (cm.getvotePoints() >= 5){
cm.gainItem(1462039, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 115){
if (cm.getvotePoints() >= 5){
cm.gainItem(1472052, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 116){
if (cm.getvotePoints() >= 5){
cm.gainItem(1482013, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 117){
if (cm.getvotePoints() >= 5){
cm.gainItem(1492013, 1);
cm.gainvotePoints(-5)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 118){
if (cm.getvotePoints() >= 10){
cm.gainItem(01302086, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 119){
if (cm.getvotePoints() >= 10){
cm.gainItem(01312038, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 120){
if (cm.getvotePoints() >= 10){
cm.gainItem(01322061, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 121){
if (cm.getvotePoints() >= 10){
cm.gainItem(1332075, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 122){
if (cm.getvotePoints() >= 10){
cm.gainItem(1332076, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 123){
if (cm.getvotePoints() >= 10){
cm.gainItem(1372045, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 124){
if (cm.getvotePoints() >= 10){
cm.gainItem(1382059, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 125){
if (cm.getvotePoints() >= 10){
cm.gainItem(1402047, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 126){
if (cm.getvotePoints() >= 10){
cm.gainItem(1412034, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 127){
if (cm.getvotePoints() >= 10){
cm.gainItem(1422038, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 128){
if (cm.getvotePoints() >= 10){
cm.gainItem(1432049, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 129){
if (cm.getvotePoints() >= 10){
cm.gainItem(1442067, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 130){
if (cm.getvotePoints() >= 10){
cm.gainItem(1452059, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 131){
if (cm.getvotePoints() >= 10){
cm.gainItem(1462051, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 132){
if (cm.getvotePoints() >= 10){
cm.gainItem(1472071, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 133){
if (cm.getvotePoints() >= 10){
cm.gainItem(1482024, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
}
}else if (selection  == 134){
if (cm.getvotePoints() >= 10){
cm.gainItem(1492025, 1);
cm.gainvotePoints(-10)
cm.dispose();
}else{
cm.sendOk("you dont have enough vote points!");
cm.dispose();
        }
     }
   }
 }