//#include <bits/stdc++.h>
#include <iostream>
#include <fstream>
#include <time.h>
#include <cstdlib>
#include <stdlib.h>
#include <math.h>
#include <map>
#include <cstdio>
#include <cstring>
#include <iomanip>
#include <string>
#include <string.h>
#define N 1000000000
typedef long long ll;
typedef unsigned long long ull;
using namespace std;
bool a;
#define voters 99999999
//ll variants=11;
ll results[11];
ull arr_h[16];
ull arr_h2[16];
ll c, d, n, fi, nrndv, r, r2, cnt, all=0;
ll pass[voters];

char FileName1[20] = "nrndv.n";
ifstream fin1(FileName1, ios::binary);
ofstream fout1(FileName1, ios::binary);
char FileName2[20] = "nrndv2.n";
ifstream fin2(FileName2, ios::binary);
ofstream fout2(FileName2, ios::binary);
char FileName3[20] = "nrndv3.n";
ifstream fin3(FileName3, ios::binary);
ofstream fout3(FileName3, ios::binary);

int gcd(ll a, ll b) {
    ll r;
    while (b!=0) {
        r=a%b;
        a=b;
        b=r;
    }
    return a;
}

int gcd3(ll a, ll b) {
    ll q;
    ll A[2], B[2], T[2];
    A[0]=a; A[1]=1;
    B[0]=b; B[1]=0;

    while (B[0] != 0) {
        q=A[0]/B[0];
        T[0]=A[0]%B[0];
        T[1]=A[1]-(q*B[1]);
        for (int i=0; i<2; i++) {
            A[i]=B[i];
            B[i]=T[i];
        }
    }
    return A[1];
}

ull bvs(ull num, ull step, ull md) {

    ull y=1, s=num;
    while (step != 0) {
        if (step%2 == 1) y=(y%md*s%md)%md;
        s=(s%md*s%md)%md;
        step /= 2;
    }
    return y;
}

bool ferma (ll p) {
    if (p == 2) return true;
    if (p&1 == 0) return false;
    for (int i=0; i<100; i++) {
        ll a = rand() % (p-1) + 1;
        if ((gcd(a,p) != 1) || (bvs(a, p-1, p) != 1))
            return false;
    }
    return true;
}

bool check () {
    ull pid;
    cout << "Your PASSPORT ID: ";
    cin >> pid;
    if (pass[pid]!=1) {
        pass[pid] = 1;
        cout << "You can vote!" << endl;
        return 1;
    }
    cout  << "You cann't vote anymore!" << endl << endl;
    return 0;
}

ll gen_rsa() {
    ll checker=check();
    if (!checker) return -1;
    ll p, q;
    char m;

    while (true) {
        q = (rand()<<16)|rand();
        q=q%60000;
        if (ferma(q) == 1) break;
    }
    while (true) {
        p = (rand()<<16)|rand();
        p=p%60000;
        if (ferma(p) == 1) break;
    }
    n=p*q;

    fi=(p-1)*(q-1);

    while (true) {
        c = rand()*rand()+rand();
        if (c&1 == 0) continue;
        if (gcd(fi, c)==1) {
            d=gcd3(c, fi);
            if (d<0) d+=fi;
        }
        if (bvs(c*d, 1, fi)==1) break;
    }
    ull rnd =  rand()%10000;
    //ull v = ((rand()<<16)|rand())%variants;
    ull v;
    cout << "Your vote [1-10]: ";
    cin >> v;
    results[v]++;
    fout3 << rnd;
    fout3 << v;
    fout3.close();
    ull nrndv;
    fin3 >> nrndv;

    while (true) {
        r = rand()*rand()+rand();
        if (r&1 == 0) continue;
        if (gcd(r, n)==1) {
            break;
        }
    }

    r2 = gcd3(r,n);
    if(r2 < 0) r2 += n;

    fout1 << nrndv;
    fout1.close();
    fout2 << nrndv;
    fout2.close();

    cnt=0;
    while (1) {
        fin1.read((char*)&m, 1);
        if (fin1.eof())
            break;
        cnt++;
    }
    fin1.close();

//	cout << "fi=" << fi;
//	cout << endl << "c=" << c;
//	cout << endl << "d=" << d;
//	cout << endl << "rnd=" << rnd;
//	cout << endl << "v=" << v;
//	cout << endl << "nrndv=" << nrndv;
//	cout << endl << "r =" << r;
//	cout << endl << cnt << endl;


    return cnt;
}
//
//void md5() {
//
//    char data[11];
//    char m;
//    for(int i=0; i<11; i++) {
//        fin2.read((char*)&m, 1);
//        if (fin2.eof())
//            break;
//        data[i] = (char)m;
//    }
//    fin2.close();
//
//    HCRYPTPROV dhCryptProv;
//    HCRYPTHASH dhHash;
//    CryptAcquireContext(&dhCryptProv,NULL,NULL,PROV_RSA_FULL,0);
//    CryptCreateHash(dhCryptProv,CALG_MD5,0,0,&dhHash);
//
//    CryptHashData(dhHash, (BYTE*)data, strlen(data), 0);
//    DWORD dcount = 0;
//    CryptGetHashParam(dhHash, HP_HASHVAL, NULL, &dcount, 0);
//    unsigned char* dhash_value = static_cast<unsigned char*>(malloc(dcount + 1));
//    ZeroMemory(dhash_value, dcount + 1);
//    CryptGetHashParam(dhHash, HP_HASHVAL, (BYTE*)dhash_value, &dcount, 0);
//
//    ll i=0;
//
//    for(unsigned char const* p = dhash_value; *p; ++p) {
//        arr_h[i] = (unsigned(*p));
//        arr_h[i]%n;
//        arr_h2[i] = (bvs(r,d,n)*arr_h[i])%n;
//        i++;
//    }
//}

bool md5_s2(ll s, ll i) {
    ll correct=1;
    if (arr_h[i] == bvs(s,d,n)) correct*=1;
    else correct*=0;
    if (correct==1) {
        all++;
        return 1;
    }
    else cout << "ERROR" << endl;
    return 0;
}


void md5_a1(ll s2, ll i) {
    ll s=0;
    s=(r2%n*s2%n)%n;
    md5_s2(s, i);
}

void md5_s1() {
    ll s2;
    for (int i=0; i<16; i++) {
        s2=bvs(arr_h2[i],c,n);
        md5_a1(s2, i);
    }
}



int main() {
    srand(time(NULL));
    ll can_vote;
    for (ll i=0; i<voters; i++) pass[i]=0;
    for (ll i=1; i<=11; i++) results[i]=0;
    while (true) {
        cout << "Do you want to vote? [1=YES /  0=NO] >>> ";
        cin >> a;
        if (a==0) break;
        can_vote = gen_rsa();
        if (can_vote == -1) continue;
//        md5();
        md5_s1();
    }
    cout << "Results: " << endl;
    for (int i=1; i<11; i++) {
        cout << setw(2) << i << " --- " << double(double(results[i])/double(all)) << "%" << endl;
    }
    return 0;
}
