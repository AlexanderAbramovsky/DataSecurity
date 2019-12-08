package com.example.cardexchange

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Lab3 {

    private static final String FILE_ENCRYPT = "./fileEncrypt.txt";
    private static final String FILE_SIGNATURE = "./fileSignature.txt";

    private static long p, g, n, c, d, y, r, a, q;

    public static void main(String[] args) {

        System.out.println("RSA");
        signatureRSA();
        checkSignatureRSA(n, d);

        System.out.println();

        System.out.println("Elgamal");
        signatureElgamal();
        checkSignatureElgamal(p, y, g, r);

        System.out.println();

        System.out.println("GOST");
        signatureGOST();
        checkSignatureGOST(p, q, a, y);
    }

    public static void signatureRSA() {
        try ( BufferedWriter fileEncrypt = new BufferedWriter(new FileWriter(FILE_ENCRYPT));
              BufferedReader fileSignature = new BufferedReader(new FileReader(FILE_SIGNATURE))) {

            long count = 1_000_000_000;

            long p, q, n, fi, c, d, e;

            do {
                p = Lab1.generatePrimeNumber((long) Math.sqrt(count) * 2);
                q = Lab1.generatePrimeNumber((long) Math.sqrt(count) * 2);
                n = p * q;
            } while (n < count);

            fi = (p - 1) * (q - 1);

            do {
                d = (long) (Math.random() * (fi - 1));
            } while (Lab1.gcd(d, fi) != 1);

            c = Lab1.gcdGeneralized(fi, d).y;
            if (c < 0) c = c + fi;

            System.out.println("Р—Р°РїРёС€РёС‚Рµ РєР»СЋС‡Рё РґР»СЏ РґРµРєРѕРґРёСЂРѕРІР°РЅРёСЏ: ");
            System.out.println(n + ", " + c);
            Lab3.n = n;
            Lab3.c = c;
            Lab3.d = d;

            for (byte b : md5Custom(fileSignature.readLine())) {
                long test = Lab1.pow(b, c, n);
                fileEncrypt.write(test + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("ERROR");
        }
    }

    public static void checkSignatureRSA(long n, long d) {

        try ( BufferedReader fileEncrypt = new BufferedReader(new FileReader(FILE_ENCRYPT));
              BufferedReader fileSignature = new BufferedReader(new FileReader(FILE_SIGNATURE))) {


            long e, m;

            String str;

            byte[] h = md5Custom(fileSignature.readLine());

            byte i = 0;
            byte count = 0;
            while ((str = fileEncrypt.readLine())  != null) {
                e = Long.parseLong(str);
                m = Lab1.pow(e, d, n);
                if (m == h[i++]) {
                    count++;
                }
            }

            if (count == 16) {
                System.out.println("РџРѕРґРїРёСЃСЊ РІРµСЂРЅР°");
            } else {
                System.out.println("РџРѕРґРїРёСЃСЊ РЅРµ РІРµСЂРЅР°");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());

    }

    public static void signatureElgamal() {
        try ( BufferedWriter fileEncrypt = new BufferedWriter(new FileWriter(FILE_ENCRYPT));
              BufferedReader fileSignature = new BufferedReader(new FileReader(FILE_SIGNATURE))) {

            long q, p, g, x, y, k, a, b;

            do {
                q = (long) (Math.random() * 1_000_000_000) + 2;
                p = q * 2 + 1;
            } while (!Lab1.theoremFerma(q, 100) || !Lab1.theoremFerma(p, 100));

            g = Lab1.getG(p, q);
            x = (long) (Math.random() * (p - 1))  + 1;
            y = Lab1.pow(g, x, p);
            k = (long) (Math.random() * (p - 2))  + 1;

            do {
                k = (long) (Math.random() * (p - 1) + 2);
            } while (Lab1.gcd(k, p - 1) != 1);

            long kInverse = Lab1.gcdGeneralized(p - 1, k).y;
            if (kInverse < 0) kInverse += p - 1;

            long r = Lab1.pow(g, k, p);

            System.out.println("Р—Р°РїРёС€РёС‚Рµ РєР»СЋС‡Рё РґР»СЏ РґРµРєРѕРґРёСЂРѕРІР°РЅРёСЏ: ");
            System.out.println(p + ", " + y + ", " + g + ", " + r);
            Lab3.p = p;
            Lab3.g = g;
            Lab3.y = y;
            Lab3.r = r;

            for (byte by : md5Custom(fileSignature.readLine())) {

                //TODO СѓР·РЅР°С‚СЊ РїРѕС‡РµРјСѓ abs
                long u = ( Math.abs(by) - (x * r)) % (p - 1);
                u += u < 0 ? p - 1 : 0;
                long s = (kInverse * u) % (p - 1);
                fileEncrypt.write(s + "\n");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void checkSignatureElgamal(long p, long y, long g, long r) {

        try ( BufferedReader fileEncrypt = new BufferedReader(new FileReader(FILE_ENCRYPT));
              BufferedReader fileSignature = new BufferedReader(new FileReader(FILE_SIGNATURE));) {

            byte[] h = md5Custom(fileSignature.readLine());

            String str;
            byte i = 0;
            byte count = 0;
            while ((str = fileEncrypt.readLine())  != null) {

                long e = Long.parseLong(str);

                //TODO СѓР·РЅР°С‚СЊ РїРѕС‡РµРјСѓ abs
                long t = Math.abs(h[i++]);
                long test = (Lab1.pow(y, r, p) * Lab1.pow(r, e, p)) % p;
                long res = Lab1.pow(g, t, p);

                if (test == res) {
                    count++;
                }
            }

            if (count == 16) {
                System.out.println("РџРѕРґРїРёСЃСЊ РІРµСЂРЅР°");
            } else {
                System.out.println("РџРѕРґРїРёСЃСЊ РЅРµ РІРµСЂРЅР°");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void signatureGOST() {

        try ( BufferedWriter fileEncrypt = new BufferedWriter(new FileWriter(FILE_ENCRYPT));
              BufferedReader fileSignature = new BufferedReader(new FileReader(FILE_SIGNATURE))) {

            long q, p, b, a, g, x, y, k, r;

            long poW = (long) Math.pow(2, 31);
            long poS = (long) Math.pow(2, 16);


            do {
                q = (long) (Math.random() * 32768 + 32768);
            } while (!Lab1.theoremFerma(q, 1000));


            while(true) {
                b = (long) (Math.random() * q + 32768);
                p = b * q + 1;
                if(Lab1.theoremFerma(p, 1000) && p <= poW)
                    break;
            }

            do {
                g = (long) (Math.random() * (p - 1) + 1);
                a = Lab1.pow(g, b, p);
            } while (a <= 1);

            x = (long) (Math.random() * q + 1);
            y = Lab1.pow(a, x, p);

            System.out.println("Р—Р°РїРёС€РёС‚Рµ РєР»СЋС‡Рё РґР»СЏ РґРµРєРѕРґРёСЂРѕРІР°РЅРёСЏ: ");
            System.out.println(p + ", " + q + ", " + a + ", " + y);
            Lab3.p = p;
            Lab3.q = q;
            Lab3.a = a;
            Lab3.y = y;

            long s;
            for (byte h : md5Custom(fileSignature.readLine())) {

                while(true) {
                    k = (long) (Math.random() * q);
                    r = Lab1.pow(a, k, p) % q;

                    if(r != 0) {
                        s = (k * h + x * r) % q;
                        if(s != 0) break;
                    }
                }

                // System.out.println(s + " " + r);

                fileEncrypt.write(s + " " + r + "\n");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void checkSignatureGOST(long p, long q, long a, long y) {
        try ( BufferedReader fileEncrypt = new BufferedReader(new FileReader(FILE_ENCRYPT));
              BufferedReader fileSignature = new BufferedReader(new FileReader(FILE_SIGNATURE))) {

            long r, x;
            long s, hInverse, u1, u2, v;

            byte i = 0;
            byte count = 0;
            byte[] h = md5Custom(fileSignature.readLine());

            String str;
            while ((str = fileEncrypt.readLine())  != null) {

                s = Long.parseLong(str.split(" ")[0]);
                r = Long.parseLong(str.split(" ")[1]);

                if((r > 0 && r < q) && (s > 0 && s < q)) {

                    hInverse = Lab1.gcdGeneralized(q, h[i++]).y;
                    if (hInverse < 0) hInverse += q;

                    u1 = (s * hInverse) % q;
                    u2 = (-r * hInverse) % q;
                    u2 += u2 < 0 ? q : 0;
                    v = ((Lab1.pow(a, u1, p) * Lab1.pow(y, u2, p)) % p) % q;

                    if(v == r) {
                        count++;
                    }
                }
            }

            if (count == 16) {
                System.out.println("РџРѕРґРїРёСЃСЊ РІРµСЂРЅР°");
            } else {
                System.out.println("РџРѕРґРїРёСЃСЊ РЅРµ РІРµСЂРЅР°");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static byte[] md5Custom(String st) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();

            for (int i = 0; i < digest.length; i++) {
                digest[i] = (byte) Math.abs(digest[i]);
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("ERROR");
        }

        return digest;
    }

    public static threeNumbers gcdGeneralized(long a, long b) {
        threeNumbers temphere = new threeNumbers(a,1,0);
        threeNumbers temphere2 = new threeNumbers();

        if(b == 0) {
            return temphere;
        }

        temphere2 = gcdGeneralized(b, a % b);
        temphere = new threeNumbers();

        temphere.d=  temphere2.d;
        temphere.x = temphere2.y;
        temphere.y = temphere2.x - (a / b) * temphere2.y;

        return temphere;
    }
}
