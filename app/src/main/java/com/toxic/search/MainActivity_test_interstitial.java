package com.toxic.search;

import android.app.*;
import android.animation.ValueAnimator;
import android.os.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.webkit.*;
import org.json.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.UnfetchedProduct;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends Activity {
    private static final String PROFILE_API = "https://atoxic.com.br/api.php";
    private static final String HABBODEX_BASE = "https://habbodex.com/api/v1/habboinfo";
    private static final String HABBODEX_FURNIDEX_API = "https://habbodex.com/api/v1/furnidex/furni/from-figure-string";
    private static final String APP_VERSION = "1.3.38-test-interstitial";
    private static final long PROFILE_MIN_LOADING_MS = 0L;
    // Cópias exatas dos ícones atualmente usados pelo iframe do HabboNews.
    // A API fornece apenas o hash; o APK usa estes arquivos locais para que
    // os ícones nunca desapareçam por bloqueio de rede ou cache externo.
    // Índice técnico embutido do HabboNews: liga cada código de roupa (ex.: ch-240)
    // ao mesmo ícone de raridade usado no iframe. Ele era aplicado no api.php;
    // como o perfil agora consulta o HabboDex diretamente, o índice passou para o APK.
    private static final String HABBONEWS_TRANSPARENT_ICON = "OuxYRCz";
    private static final String HABBONEWS_RARITY_SEED_GZIP_BASE64 =
            "H4sIAAAAAAACA3WdSc9lN3Jg/0uuKwEyRlK7gntYNNyD7QYM71SpzJJcUqNgVVc2YNR/bzzetN69ZBzgW3yI80gGg8F5uP/+4a8fvuu/+/DHz//n8799/5fP" +
            "P/z+Lx+++yBN4mMbH1v/p9a+W3//8uF3H376y+dffv3w3b9/+PHfPvbW1n8fvut/+90l8E3Q91/0/ReyC3QX2C7wXRC7ILdkvR2CLYjvmvquqcv+C90Fdgj2" +
            "OHz/xZ4Xz10w4ymIPAS5C8YumJtg9KdgNNkEff+FjF2wRTp2ewzd49A9FdVdYLvAd0HsgtwFu6a6a7oX1LBdU9s1tV1T2zXdC3vYrqntmtquqe2a7g4zfNfU" +
            "d01919R3TXenG75r6rumvmvqu6axaxq7prFrGrumsWu61+wRu6axaxq7prFrurcOI3dNc9c0d01z13Tsqcw9lbmnMvdU5paKtma7ZK+X2rrskr2l0iZHKDli" +
            "3ltibXvN0mZHzHbEbEcu7Ehrd3xtuwtq2w2oPU5JPyRySHZ9eh6/mXZIdmvIYUPZ67OKH7/xI54jp3LkS3Z/U9nbdJW9DVeVla+fPnz3Ib//+V+7/f2H34gT" +
            "udrGilxtQEUCw1xVqyQY2wgiV30oCYUxRXI1DBWZlB/vv8X2z//l6z/7py9vgmGkIaFS8KunrAhq7VgK0QS0jkaxxVu3PQz6QVzNdUkop/H2kJ/mv/zf//zz" +
            "f3qTjmEUCWqQQem8S3sj2RwJ+UG2JNJJt+yYjhgSsmi+fWfXQJWIUS3Jd5nuYdAT810K+um/25/+/n++SRIZ5KM5HAlqPSaFmQ0J+dtoVINHZ0KlMDrp9m0E" +
            "XcVmqJuRJ34bXVbEf7P1v/70+Q8//FV+I0HW+TbQqgi28QNr8BRKZwqV6VTSYBrVrOlkg5lIBtWSib4zJ2owoZZYo37BmkCds0b9grVJsfWeSEi3LmBr69S6" +
            "WH/76Pf/+6v+/L/+65sohQnUOoTI6HUbYp3aEOsD80NjChPq603ePe0P/6/99fd/0t/Iuy4cROuW3CSNyNt3Ng30bdHNBupkA33beg8TGFuCx9t8l/azDbH5" +
            "bke//OMf/vH3f/j1TcCi3vpAAm2iN4VS8G+zkoL0NmutvVOL5P1dG/fY3n3wTkwotiQbCPUlLkbWEWphXdKJDCTkb6403nEVsrVKJ0KtmCv1P65JFtUBNct1" +
            "DNJgkO/opPxYI+uYJoXRgYQ0MPutfP7b1//xhy9/9/1vJJw0oDbRvSmk49QvuBv0P/5tebKKzWF87f4eOexhIknrpFrvWHKOJedvv97Suc2MtnTiPfY/wgSF" +
            "QYsGtmKBvhPoO4HWidGQUJ3LTvU0uxDB1jLRQ9LJDzKpFDKV0pmYnwmzKR8Nxi5+G19vuo1B5TNoZOezCxKqWVOMwqgSCUyH1gJ8Yo8xydbReiMioFs0hV4z" +
            "mjoRd4otlMLkIDJQt2H1CCUa9Y2BI+LokkQSSjuEZoch1J+GUH8aOB4N7WQDpdWI0E4WVSxtNbKbog10QmsZRjOjMOlElPJjTlrf+tMtp0Z9VlgkkYmxTYrN" +
            "G9Usb2RRf49D/ulP7ad/+Af/jVBLMf25jfbDxz7aLvBNMPdfzO0X0touOH4RuyB3wdgF8ymIPZXYU4m+/6Lvv5D9F3L8InZB7oKxCzZNtT13dJZEDokeEjsk" +
            "fkjikOQhGYdk17DvxtTe+iGRQ6KHxA6JH5JdZ3+v4NxXCBZJIFGvvS2iSIxIPfP44bVuW87CX0TKtvqH10qeQpjRKbZRt5Q/vNaqgkjdUv7w0Vr3Wje79cxP" +
            "i1oPsJv1BLuZtHIkuIgiMSSOJIgEk0QyiOQkMhoSSkcHWNSbIqlHnIuArf22jrYTZwKl4NoDSSIZSMCibgoWda/3JxcxDAO+4zEotsDyiWFInMiEeupplNPE" +
            "0s45kJBFhyaSgQRjQ63HwNgGxjYpttmhFKI1sE40aneiUbsTjdqdaNTuRKN2J1pLJAPJJCKdbEB1IZpgfgTzI5gfwfwYxmYYm3FsaJ2knHZPJBRbd7L1bf1+" +
            "i02oFQvpGBvmRwaGmdCGhAaMKcK0XP1bJJCQbka1PoxqfRi1VWEeoLW/a8ltTvLpx4/yGJsvgW8C2X8h+y90/4Xuv7D9F7b/wvdf+P6Lx0GhJTh+Ebsgn4LY" +
            "NY1d09g1jV3T2DWNXdOI/Rdx/GJX7HHm7SXIPY7c4xj7L8b+i7n/4jH/+fTjx/GYOCyB7QLfBbELcheMXbBlbnTZBbsefddjd8vRdz36rkff9ei7HrtrD9kN" +
            "JLumsmsqu6Z79Riya7oX5cjjF3tecs9L7nnZ3WGMPS9jz8vY8zL2vOwupa31Q3L8Zi9NbXtxautHqN3U2vbaqk3P3xxpaRySsUvsSN2OUDZ3iR/6+GENP0LF" +
            "EWqv/drySH33AO17LdK+t93aez8kckj0kOyl0w+r9r2h1K6HhnH85shpH0fMh4/1eYSae95lb7lU9mZH5cipHDk9ujuVww/lsIboEc9hDbHjN4fXHb2eHn2Y" +
            "Sh55n7s+Wg5bL6JEVJAYEkcSRKwhQQ0c8xOJZBBJjC1Rg/KI6YtYo/xYDySktclAghpgyZkqEkxHMR0sHzNMx9AGjukMDDPQbgNjm1g+syPBnE7UbaJuk2zt" +
            "R+9ZbyNchFL23pAkEtRJMIxwGLK+K+qGHujogY5th0e1GbrIqI65XUSIcIlhOxDdkURxiOkiiWEGhZHq+PMi2pAEkiTiZIPAmhvvNnePLVDr8qDQRVCDRA2G" +
            "IFEkXmwvXySQYPmMCSRlQE5TJhF1sFuWx4UvgukE2S2zIUHdRkOCGmDLmlORkPfmJO8d2FqOlkgGErLb6FQKo5MNhhoSR4L5wdZyYGs5DHNqGJtTCzsc8xNo" +
            "g4G6DeqV6wXpi9AIdQppPbVDPZ0qSAxjo5KbRq3y7UjuQQSJIkkkAwm1yrcDSbfjdItkQ9LJBtjGT2xdJrYuc2A6I5GQJ85J/faksZ61BrpZax2J1n2J3TZA" +
            "diJOGoRQOjQSskZzJWs0V7JG/ba1bEg6EkGiSIzISCSDyGQCLZ/1RjntrVMYGpNb7xhGyG5dBAl4vHUabVh/9xjPca/1d1t1ECHiTumU2w8XIU/sNOq0Ho4k" +
            "kJD3dvTRjj7ax6T8zIYE2lGT1oiU210XmUTKY4gX6UgEiSIxJI4kkCQStIGgDRRtoGgDRRso2kDRBoo2ULSBog0UbaBoA0MbGNrA0AaGNjC0gaENDG1gaAND" +
            "GxjawNEGjjZwtIGjDRxt4GgDRxs42iBhDmiSQWRgmMFhqLWUSS2fKrXkSus7pjRjMVwbNnVqYW9rwwehkdBtO39re/U9rjoI9T+K/Y+WF2YuMogMGg0qrVOY" +
            "0qzalGbVNrGWTKwlM2jsUl9tWCSplsx0JAHWuY399zBDkCgS1G1Qvz3R1pNtPQ1j85p4ffjpIkGkw1jZW3ciAvXHG61KedOGBGbi3mivyJtifqhv9EZ9ozdD" +
            "DQw1sEQyiISQBqFIDAnaINAGgTbIhqQjwfzQPok3qgveqC54m1g+E8uHZuKO8yzHeZZ3WnX3TrsiXh9bWyQako5EkMCcyXskkkGEdsW8v0vudjjtIl4fDfPb" +
            "jGXTWlpHAusUfjtufhBHQn4gtKrr0gWJIjEkjgR166wblY9IQ0K+g7M2x1mb46zNcZ7lt9nH5ju32cdBsEyNPB7Hyo5jZRcfSDA/WIMF66lgGy+BpU2rbK5N" +
            "kJCPKrZ8ii2f0pzWlea0rgbzBVdacXaldWW/jeMPglo7tW/qNELRwNgCbY0trGILq7Ru6Urrlq4TbT0dSSChumA0d3ajubObk3Xqa5mL0Kq7G626u79b2K2l" +
            "8O5IqIV1WmFypxUmd1phcseWz2mFyZ1WmNyd6oJHQ4KxDUeCNhhog4E2wLGY41gssA8O2ln125mF516bB+3PeajCnCnUiND6jget73jQ+o4Hre94YJ8V2GcF" +
            "9lmBfVZgnxXoO4G9WWBvFjhjiUAb4FwmcC4TE0sOZ8hBq1KetD/n2Smnt+f+DuJIKKeJs92knXxP7JkyqO29nafYrHM7T7GR0RxJEHnX0+fakw/tSGD9zfEE" +
            "hA+sPwPrz8D6M7D+DKw/A+vPwPozsP4M2lX0SedGfdKuok8cJ04cJ85mSBxJIEkk1MZPnP/MzmHQbjgzmoJ2E7SboN0U08Ex0sQx0nQsBcdScCwFR4s6WtTR" +
            "onRiwGdQPZ0T0yGPj9ZhpTFaFySOsQURQQ2oTAPXE6PRXls0x/zQjCUazViiBepGLVLgGmS0gdahM9TRaCYRbaJ1Jlmn02pR9E7pdKrB0akGR6c91+i0rhzd" +
            "UIMku/VMJORvnU65hVAbH0JtfIgwUSK0GhG4GhFC+zIhEzWg9d7A1YhQWpkLpTY+8GZLKJ1bDzUmAwmV6W0F4yCom2NOHe1GPUbo+1z0c8QV+vbEndBMPG73" +
            "V7Yw1joRGiuH0Vg5jMbKYdgeGJ2ECaOTMGE0Rw+jOXoYnQIJoxWzMOppA2+2hNEIP4xG+GHYIhm2SIa10Wj3JfD+SuD9lXD0EH+3Yjt5r1s+9xfCadU9HEcB" +
            "uCYUePclnFbDo36s5Oc/fpTH9esl8E0w9l887tsuwRYk5iGIpyBb2wVbkOz7L/rxiz1S2YKMx9NZP//xdYc3dkk/fvO4Tn1J5i553I5eEjvi8VNyxBx9l+Tx" +
            "m7FL+p5z7Y97mpfk/M0RTxy/ySOe2Q5JPyRySPa0ZC9xldYPyR6PHDl9PrmwJIedn88hLMk4Yh57CcqRUz00rPvviwwi5V3FiySR8pn9RRxjK+dbiwTGVn4o" +
            "4yIY28AwA/MzSQNrTBIJ2do6EiWtrRwRLRJU2jaoFPzwYRfKoZefGFhkErmtRd/OKy8i1X7WIpPKMssbBxch64/ytuwi6E2zkwaz/MTAIu96c1snX6Tca1uk" +
            "PH328x9fdxGkTsfq2ftFIKfWehIhP7MWQWT0Oj/WhiBRIpO07o1i610pTHny6iKTwohTGB1g6x5k0T6pfLSRRbWRboplquSjhq2xqQ8i5T2jRVKRkAaTvMpv" +
            "z3U9reO9PE23yLu0bzuRFwG7eS/3mRYp14gv4qTbbKBBfaZ+kfLjHYuU60gXMSSOhDRA33FVso6GUphy7ekiE4gb1Cx3CwyTSEiD2w3yp4+6D7KolztdLxJC" +
            "HhLljcQXSSVbpzmkM6nW+5Qkkg0J1LlorRHp4KOBPUbc7nQdBPITXZSIQmlHp1FgSPlU4otoUyI02gyjWhJG44OwJBsY9Wbh7377Nt9ehPwt6vMUv/748Tn4" +
            "XwJ/CrS1XbD9ImUXaNsF+y9s+8XcI52PGesS5C4Yu2I9donkLtEjf49vnF6SI+bHx0R/XW8q7fF03+N5vvF9SfbfyJ5Plcf3Y5ck/ZDs+apvZyxiSMoVyUXK" +
            "HnmRcgbw63oDZxJRis3K0c+v6wUS0u12+uU2E1hkUjrRO4QJ1DqkWgtbZJDWMaux+4tkr04NLVLOrRcpP4T26+tua7lKtkh5kn+RMAoTYDfrZZu2SLnv9eu6" +
            "ZaBEytHCIuWo5CKTSCqQXt53WaQcF/26zsAisepk9UWSSIKtXSblVGYgSSDaoG67lqfhLkIW1XKlfRH32nvdyKvcjUrBTZBQmTqWqQflNJRyGkY5DY4NPTES" +
            "arDXn1D9dZ00ojLNThqkQFvloymUz8D8DMzPSLL1QB+d6G9TKbZp0JdEIw+JRh4Sjep2dIfWMvroSKDlC21Qg8ME2tEw6mnDHMPUz6l//7E/3+i8JHJI9JDY" +
            "IfFDEockD8k4JHOXPL8IsiSHzv3QuR8690PnfujcD537oXM/dO6HznLoLIfOcugsh85y6CyHznLoLJvOevxGm9suGftv+p537XvetcvcJdoOyfEbO35jdkh8" +
            "l/gRyg+dsx8SPSS7PtIOyV6C+u2dgIckDsluHzly+u2u/UMih2TX+dtN84fEDsmRizjSCj8kh8556DOPeOYe6jm5uyR6SPbU4fORL1KezLhI+eHPF3GhMPUH" +
            "WV4kMEz9cZUXGRhmMjEgVr7xc5FJYcqV1kWkIxEkZB0rT8VdBNOx8kNYi2CYcj/iIh2JIFEkhsSRBJIkEphT9KrbmY37Z34WGUgmkdGQdCSCZFCZsl+/5ydb" +
            "Pb19Km0n772SndQfvF/EkDgR60gwNqNa4m+v2ux2+8jqQYJIYGyBsQXHlpSfbEjQOkMpnYElN1DrgVqjv3n50tIik7SOxoTa0ag/k/wi5Ynki1DdDvTrECqF" +
            "EExHqBRCyHdu+wRbWxVYs26frd21Niq5sEQykFArFt6QdCSCRCk/Tn3w7T7XQYJIYMkFtSG3+097+ST6KPYYMdCrBrVvMalVDmzjs1HvnK38UPMik0hvSDro" +
            "ljhCSewXUqlHT6VSSMOcmiChkkts/dMTyUBC9SejIelIBIkiwZxGUJkGjfATPT6TWuVEvx6NxnxDaJw4jFry4TSmGJ4Yhjx+4OxjYus/hUp7KtXgiZ54u7G0" +
            "p4Oj6Imj6Fl/Bv5Fklq+iSOHOaC0rTWIzVqD+mO3j7wdsU0KU56GW0SSYqNe0273hfYw3pB0JIJEkRgSR4Kl4GiDQK0H+I614UgCyaTymZjOpHR6I7t1aRRG" +
            "qRS6QhtiXWGUZrd7SXtsWHK3nahdt4T2wPpI0o1G3tZpbcN6eW7lIo4kkCQSGFOYlLcNLlJ+1nCRQaS8w3IRJ4J+LZMJzJBNZmAYGKGY0ljMtMH4zbSRH9Rv" +
            "yFxEkcCoxm6flN28Smn+YyodiSBRJIbEkQQS6mVuu6UHoV7mtlt6kI5EkCgS9AMbUH9uq607wRapPtm0SGKZJuk2UbdZvlWzCGowoyGh/MzyzbyLCJEBcxmv" +
            "z75dROvYHD6E+yK9IelIhAjVRr99uvbZungTR5IUG62leaOxvzca+3vLSWRgfkZHIkgUiSFxJEEeMhIJ5nQm2W0OJDB68o6e2Gn1y3v5OtNFqOS6TEpHG5KO" +
            "RJBgftSQkN067WN4N8yPO5GgMr2d298tOlHr98jhdqbzIokERkIuNCJ2oZmR16/KXwTTwdKuX0dfxMg6EuTXkkJhElbzXJJqloyOxIhM8tHbPfmDGBLKqbaO" +
            "xJBQ26tC3qsSSJKIYmxY2moNSUciSBSJIUGtaaXEb7cDN39TWgl2HCO5Yt+oybEl6ZawTuG3twI2Yg3mgG5NiXTyAxOYsbiJIFEkhsSRBJJEMoigV5k3JFRP" +
            "jUaqbjGQUHtgibElalDes7qIYxjyxPrG50UMSZAn4kjo9sLBQah39kZtiOP42juHoXbHcXztOHZxbC0dW0tXqnNOa3bugRpMzCna2mkVx0PIE+Nd57bSDuyD" +
            "A/0gJnlvTLJO4jwr6SyOJ62UeAq1BymYDva0iT1tYk+bOOLCfTNPg9U8T5qje6JFb99H3MpndJozDRUYiw2jOdMw8uthAwnqRuvxPmg93getx/ug9XgfjjYI" +
            "8pARjiSQUPlMnIFNHAlNw9iCbH1bD9nDDIxtDCSo9cR0qBWL1sGvo1ELG41a2MB9mWh0CjEa7R3GbdVjT4csGm02im3COnnc7rwfRIF0mmNEpzlGdJpjRKc9" +
            "vei0Sh29DSSTSMd0qGcKXD+IPh1JIoFaEtLI1kLnkUIwpyJka7GGpCNRJIYkkKDWjrollcJtjr6TgWGwPdDWkHAYIUKna0Od6rYGkoT1xFAaCYV1soHRTkpY" +
            "Uk4t4axUWKIGtA4bhm1V/U35Hz9/7PG8tbIkckj0kNgh8UMShyQPyTgkc5c8b618ft0Enrskj99kPyRHqHH8Zmx5125xSPZ4vq1H3iVhu+RIq49xSPaYpe+/" +
            "EclDcv7miEcPiR+hctf5eev8kuypa3kj+CKTiA8i9Yx3EYxtOJFJxIIJpXO7BfkWJf54IMEE6sZ6kY5EkKBuaH531C1Qt0DdAnWrG7cXqRu3z6/juOUi0iKD" +
            "wpggUSSGxImgdaK+IPD5dXRTIT9Zb/G8iFJ+svz8+UUwHcfYXJEYErJOJqaT5SGUz6+Dhg5aDyEPGUKeOOrjQy+CdWEE+ehIRUL1Zwyy2xiYzsSc1sfzFxEk" +
            "qPWkJnx2iu32pPx94PIiOqHkZn39aBHyg/rZm0Um2M1ah1Kw2/NUz9pozTHMBN+x22ThPqx7kXqhcxElUn5e4SKYTmA65SNUFxEkqFu9gLKIE0nUIFGDMcjW" +
            "WApK7ahpp9iUWnJT6jUNByCmoyHpSKBvtNtS0ZafaY4EegybQb4zsXxmZl0bbVKL5E0hp95oVOMty8PKLzKgPXBYjvn8Ov4AbaLfHqE6yCRSL8u9yASLev2R" +
            "80VoTOGSlB8hf3PthsSJKOVHDUZPfvPr+4bri0xoKdyozrnVy38vUj7lsgjVRr89C/7sf9wiKEz5jNyLuFN+3KE2uid5lSfpFp38Oow0CDMkjiSQJJKBhDw+" +
            "vCHpRCb5W2INTmxDknpNz6BakjT584wgDd6L/luZ5kANBtktJ9ltlI+iXoQ8cdCYwkd9iHiRRDKQTCLSkHQkgkSRkPcOoVIY9RbcIg5kdurNZqc6NwPGFD4n" +
            "tBTROnhINMpPNFUi5NfRqE2MFolkEKGxSzSaSURvSkS8rlnRlWzQlezWrSHpSASJIXEkgQTz41RyPTsStOhAMik/QiOHEFrgC1FoLUOUvEocNQhMZwYSSqd+" +
            "gu0ik4iRBkrrO6FBZarZkCgSainqZ+4vQtZRGq1H/QD+ixiNQ8KwLhj1c+FY671VR2s/f/+x22ND4pLIIdFDYofED0k8JdrGFo/2xxbFJRm7xPohOX4Tp2Tu" +
            "ksemxZLMXUMROyTHb0J2yRFzPRdeZBCpj5ItUq4lfV7PXRgSRxJIEskAEuWcbpHyMZ2LdCKTrJPlVuRFqpXqRYx0S29ISLfRqBRGeeRzkUG2HoNsPcut70XK" +
            "y9iLREPSkQgRKm1r5Zrv53WVs9c2MCmPFH5eV/jAbqYGdrNZ9gCLlB+RvkggSSSQjreWSDjMBFJ/6G8RCySUTncKoxNKwa2cHb2IJxMoHw8qbY9y3nQRIyLV" +
            "IdpFyvHlRUiDNEUya7/2UY55XmSWhyc/ryNRkE7Uz/VeBGwQ9ccXLkKx9Ql+EKKdCJV21I/lLqIYBtOpP5dwEUViSBxJIEkkAwnawNAGDr4TmmidJOuYkdb1" +
            "cftFMDaXjqQK8+X7j/350a5LIodED4kdEj8kcUjykIxDMnfJ4/DKJTl07pvO2i13ydxyoaJtl2TskrlL6jnORRxJEPFdBy0/nfBlPdYnRMrH0y7SkQgSRWJI" +
            "HEkAcad0/N1/vUWU+dvZ/luH8mW9YkUJZCODZVaDqxcZnTQYnTI/OmV+lC+gLRLkRqN8DePLGvgpxDbL9zgu4kiCSLmJ+GW9TcPEiFi1gHkRRWJIHEkgSSQD" +
            "ySTiUHWtB5IBpW1S3uhZZEBp+22Y8tTNmxsSRxJIEslAMomU74J9WTeyoZHy7llbx3u5ofFl3RLuta29HkItMqqvDn9ZdyZn3e64lt9UuogjCSSUU52kdf01" +
            "4IsYEtLNyrcFFykXvxeZUrej7uXXyhZxaBM9GpVClG/tLCIGGoQGkUkeEtT/eDaydSrFlk4lly5IMB2nMs3yeMuXdd+JdBsKLZJPo5o1k3xnDrBbtAlhon6H" +
            "YRFtSDoSIVIeHfiyplQURoy0ro/rXARjSyMymKAG1IaENopNI4lMjG1CbxbmMK4KCypTG4rEkJANvMFoI7xs+T59/7GPxyToksgh0UNih8QPSRySPCTjkMxd" +
            "8pgEXZJD537o3A+d+6FzP3Tuh8790LkfOvdNZ23DDsmWlnbtu8QPSR6hMg5J7pKxS6Sfkl1nET0ke+oyNzurlneRPq2X8RuSjkSQcDpGRDCMcBgnUm5LLmIY" +
            "WzkqXiQwTLndvsioXqn7tN5xp5z6XlfUjXJ4GwFs8Wf5xeFFyndPPq0NhmoJ/SJCpHzRYZHyENwiHqD1CPKzkWTjMQSJkgZYLrNTOrOT3aaQ1lOro5UXGUSS" +
            "PGMOKtNZvpR1kVkTaw28ym43dw+SEFvvFFsni1qXSaT8ft5FMJ1UCjO0trXVrxAtMsHjTVEDLUfqi1BdsPpbLYtQe2OaQrGVG2eLjEGxDSqF24GC3QYTWgpz" +
            "I91m+XL4RRLJQDKJOPnb9EQykJB1ZpC/1UfDFqG211t5O/YiQqSDRf32vd+dvDe0bvPZi4DveP21t4tgfspt7EWojffeGhIOI0gGkXJjZpFyvrRIom4JdcF7" +
            "ChJFYkicyKTyEbRBffN9kfLQ5SJKHiI2iZRHhi/SkQgSzKkbEirT2534rS5Iot2oD3Ytv8xzEbK10hjZ6+2+RdA66uQ7Gk7pDPJeK192X6Q3JB2JIFEkhsSR" +
            "BBGBMYWbkica9dtef0vyIo6E/MCC/Pq28bWHSatHG26ZSCaR8s3dT+t9L/I3pxGxO42Ivf46+iJavd9xEUMyMDaqP24NCYwp3LEV8+xIDAm1SD7QOlOgrYoG" +
            "o1uPRt4bnTSITn1jCMyZPNRJg6T8BNogksoU57SejTTIFkgSCbX+SSsKntg3plKdS8N0nPrgdPLrDIwtqJ4mjmoS/TozkKBuA2NDj8/hSNA6kzx+NBoR1/vh" +
            "i9Cakw8ccY1JtR5XFnyqIXEkHFsiGUjIQ6Y1JNTG3w6DbhadoyOhkdAcYJ1oHVqKaNIojEIbEo36hWjWkRgRR62p5YtGqxHRyROj06wt6ve0Fwmh2CKRDCST" +
            "CNXg6COQJJKBBHM6GxIKI41sgLOpEFoFj/oBgIuQ70j57YyLJIYZSDCn6pQO9WYhOonQyk/gfC5wPhfiMI4PKU9zXGQgmUSiIelIBIkSSSrT24tKz9Ft1F9X" +
            "ucigMLS7E0prXKE0Lwktz5MsQj1g6HQksx7DhtHKXNyO920a1O8oLzKhlwkr19++fv9R2mNX9ZLIIdFDYofED0kckjwk45DMXfLYVb0kh85901lbnhI9JLZL" +
            "xpaW9tw0VNn1Udn1UTn0+VZDHpI99dofFyl32r6uXcAEUu+xf10nLjuQLPcSFilPE3xdO1QCYUb5LtnXdTqv1WGslUeqv67vQUFOvQto7ZqQjlsHW7uVjzV9" +
            "XXPR6rtGX9fcrerTLhJAsrwUvUg5UrrIIFK+7HoRQWJISOuh4Ik+B5BoCf4W3Y1IuZewSLkytEh50u4iYIMQNSIB3htCdSGUvCrq1/sukkRQa52ktZWXST59" +
            "+vitw/+P7fVPH79dkbsLcheMp0CffcMl0UOSh+SMZ+6S3naJHr+J2CXPUycvyZ4J/XaN5yHRQ3KGGodk16fvFtRvJ9LuEjt+Y+OQ7DFLHJLRDsnxm2mHZP+N" +
            "HiWobQ9Vvz57kSRS794vYkTKB6Uu0pFMIhNjm6h1WZNfxOpTAouQdUxIA7NEQhp4SyQchqzj9UxtEUVCNrhdVToIxqYYJplgbJMJ+VvUOzsvUu/svIiWe0uL" +
            "OJF3u31fMV4kMEwCyS5IFAn5TnbyncQyTSzTVIxNyRNzkkVzkkVhRXIRRxJIyDqjPn+zSEfCsZF16m9FXATTCSqFgS3sSLROotaJWs+AWnK7rHuQgYRq42wN" +
            "SUciSBSJIXEkgSSRDCRoA2yRZkcbYK2fgroJ6mbUVt2+lLqTSd47ywdbP3163TxrROr1wEUGkklEUQOFmmUtIad2+8bGQRLJQDKJjEZkogbUl1j9jY2LOJJA" +
            "kkgGakA57aJIjEiW+5+LBJIEP+g5kEyKrXzw+EWkUzr1lycuMohM8gOpzyR+ej3zQTVLsbSVxpamvSMhr6rfub8I+Y4OqvU6yKKwW7cIjJ5smiLBdOoV/UUU" +
            "CVlnll/suAh5yCxXpy5CLV/9MN9FwOO9Ufl4o/GB9/L+0kWcSK++drmIBIWRRDKIGOrmjQjaoFNL7p1acu/UknsfHYkgUdJtGIZxJIEkkZANRKEuuKgjoTKV" +
            "+jbDIoFhOLaBZBKZDUkHotQ3uopQGFEk5L1a3vq9SBJBr7JGOTVqYd2sIwmMjcrH6hsdn17n8cgGjq3L7fm9gySSgYQ85PapkV3rpNL2pNL2+kziIh1ICHli" +
            "oL/dPijyXKfwkAkkaazsqUbEYCzmaUkkyKsG1rkxKaeT1gJ8GvnONCrtiT3GpNm7zwgkSYTzMxWJIYGcRkvIacAtgxfpDUlHwrEpEkMyiKhRfgJGxNFDkRgR" +
            "Wl2JPkhroRY2pFndXoc0J0I9bQitgoZSmxhKbWIolrZS6x+GfmBCtjYa84XRmC+MxnxhQn6Na97TY9sbmt9e+vkPyZ8/an/eSH5JpB2SfkjkkOghsUNypP54" +
            "GPWS5CEZm+T5GNgl8UOyxyPjCDWOUCMOyRnPro8eaWk7f7PbWXs7JP2QyCHRQ2KHxA9JHJJd5/rb9hfRmnj91N2LWPmexSKBpG7vF3EkgSSBjHq0soghcSSB" +
            "hDUYSCaR8lTERToSQYI2GGiDgTYYZIOJfjANwySTRDKQgEWjke8E3Mb78+sM4EBC6QiVXAiVXAiVXMB8cBHSTbsjCSSUjlINDg1FYkhIt1uLdB8rLKJIDIkj" +
            "qeadf/7L6/ZNVaaLlJ8Lu0gnUj7u/iJZPjf7IqPcN1iEw5R3fV/k9pDzM6fRFLSOFpNINiRZax29TyJOsfUIClOu371IvWK9CMYmk3TT8lW4RagUQpPSqU+E" +
            "XEQhjGE6VrYHFyHr1CcoFin3uhcpV4V/eZ08Kc85vsjtJYHb3PtFsny95Ze1N5wQZpZzlF9eu0TlY/CLlO+rvUj9ibEXqe8Hv4iW7xL88lrrCdDAbY7abu7l" +
            "/GmRPiA2L2vji2TZm73IKPdOLlKtey7SGxFxSkdItzEpTP1Q/SImRCinUX+47pc1w4Z0QjqFEYXyCSnX/BYpb8RfBGwQ9WO9i1BpR/0xwhexctfrIpMI2uD2" +
            "ZtPTd6J+mWkRo5zWq6gXGUjIBoa2NhfSOjppHajBwHQG5ee2vnr7WMoixez/b3/7/4RHUQmqMgEA";
    private static final long JSON_RESPONSE_CACHE_TTL_MS = 5L * 60L * 1000L;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final ExecutorService profileSectionsExecutor = Executors.newFixedThreadPool(6);
    private final ConcurrentHashMap<String, CachedJsonResponse> jsonResponseCache = new ConcurrentHashMap<>();
    private static volatile JSONObject habbonewsRarityItemsMemory;

    // Transporte HabboDex via WebView: mantém uma sessão web real no próprio aparelho.
    // O WebView fica oculto (1x1 px) e só é exibido se houver uma verificação interativa.
    private WebView habbodexWebView;
    private FrameLayout habbodexWebHiddenHost;
    private Dialog habbodexVerificationDialog;
    private final Object habbodexWebSessionLock = new Object();
    private volatile CompletableFuture<Boolean> habbodexWebSessionFuture = new CompletableFuture<>();
    private volatile boolean habbodexWebChallengeDetected = false;
    private volatile String habbodexWebLastChallengeUrl = "";
    private final ConcurrentHashMap<String, CompletableFuture<String>> habbodexWebRequests = new ConcurrentHashMap<>();
    private final String habbodexWebBridgeToken = UUID.randomUUID().toString();
    private final AtomicInteger habbodexWebRequestSeq = new AtomicInteger(0);
    private static final long HABBODEX_WEB_BOOT_TIMEOUT_MS = 12_000L;
    private static final long HABBODEX_WEB_INTERACTIVE_TIMEOUT_MS = 45_000L;
    private static final long HABBODEX_WEB_REQUEST_TIMEOUT_MS = 18_000L;

    private FrameLayout screen;
    private final WeakHashMap<View, int[]> safeAreaPaddingByView = new WeakHashMap<>();
    private LinearLayout root, resultWrap;
    private EditText searchInput;
    private Button searchBtn;
    private TextView statusText;
    private ProgressBar progress;
    private FrameLayout loadingSkeletonProgressBar;
    private LinearLayout suggestionsBox;
    private ScrollView suggestionsScroll;
    private int suggestionRequestId = 0;
    private Runnable suggestionDebounceTask;
    private boolean suppressSuggestions = false;
    private boolean programmaticSearchTextChange = false;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private int avatarDirection = 2;
    private ImageView currentAvatarImage;
    private String currentProfileFigure = "";
    private String currentAvatarProfileKey = "";
    private View profileAvatarTutorialTarget;
    private View profileFavoriteTutorialTarget;
    private View profileFriendTutorialTarget;
    private boolean currentProfilePrivate = false;
    private volatile int activeSearchToken = 0;
    private volatile boolean searchInProgress = false;
    private volatile String activeSearchNick = "";
    private String currentLoadedNick = "";
    private final Object profileProgressLock = new Object();
    private volatile boolean profileSectionsInProgress = false;
    private volatile int inlineProgressPct = 0;
    private volatile String inlineProgressMessage = "";
    // Em aparelhos mais fracos, várias respostas progressivas podem chegar quase
    // juntas. Coalescemos redesenhos completos para não destruir/recriar toda a UI
    // várias vezes no mesmo intervalo curto.
    private static final long PROGRESSIVE_RENDER_MIN_INTERVAL_MS = 420L;
    private final Object progressiveRenderLock = new Object();
    private boolean progressiveRenderScheduled = false;
    private long lastProgressiveRenderAtMs = 0L;
    private ProfileResult pendingProgressiveSnapshot = null;
    private int pendingProgressiveToken = 0;
    private ProfileResult activeRenderedProfile = null;
    // Fonte viva do perfil atual. Estados de UI que mudam durante um carregamento
    // progressivo também são gravados aqui, evitando que um snapshot posterior
    // reverta a escolha do usuário.
    private ProfileResult activeProfileSource = null;
    private String lastRememberedOpenedProfileKey = "";
    private final ArrayDeque<ProfileResult> profileHistory = new ArrayDeque<>();
    private static final int PROFILE_HISTORY_LIMIT = 25;
    private int visiblePhotosCount = 20;
    private int visibleStylesCount = 20;
    private int photosScrollX = 0;
    private int stylesScrollX = 0;
    private static final int PAGE_CHUNK = 20;
    private static final String PREFS = "toxic_search_settings";
    private static final String PREF_MAX_PROFILES = "max_profiles";
    private static final String PREF_CACHE_DAYS = "cache_days";
    private static final String PREF_MAX_CACHE_MB = "max_cache_mb";
    private static final String PREF_HOTEL = "hotel";
    private static final String PREF_OPENED_HISTORY = "opened_profiles_history";
    private static final String PREF_FAVORITES = "favorite_profiles";
    private static final String PREF_NOTIFY_FAVORITE_ONLINE = "notify_favorite_online";
    private static final String PREF_FAVORITE_ONLINE_STATES = "favorite_online_states";
    private static final String PREF_VISUAL_EDITOR_FIGURE = "visual_editor_figure";
    private static final String PREF_VISUAL_EDITOR_GENDER = "visual_editor_gender";
    private static final String PREF_VISUAL_EDITOR_TYPE = "visual_editor_type";
    private static final String PREF_VISUAL_EDITOR_DIRECTION = "visual_editor_direction";
    private static final String PREF_SAVED_VISUALS = "saved_visual_editor_figures";
    private static final int MAX_SAVED_VISUALS = 6;
    private static final int MAX_FAVORITES = 12;
    private static final String PREF_TUTORIAL_VERSION = "tutorial_version";
    private static final int CURRENT_TUTORIAL_VERSION = 6;
    private static final String PREF_PROFILE_FEATURES_TUTORIAL_VERSION = "profile_features_tutorial_version";
    private static final String PREF_FRIEND_CARD_TUTORIAL_VERSION = "friend_card_tutorial_version";
    private static final String PREF_VISUAL_ITEM_TUTORIAL_VERSION = "visual_item_tutorial_version";
    private static final int CURRENT_PROFILE_FEATURES_TUTORIAL_VERSION = 2;
    private static final int CURRENT_VISUAL_ITEM_TUTORIAL_VERSION = 1;
    private ValueAnimator tutorialPulseAnimator;
    private FrameLayout tutorialOverlayView;
    private View mainTutorialSettingsTarget;
    private View mainTutorialSearchTarget;
    private View mainTutorialVisualsTarget;
    private boolean profileFeatureTutorialRunning = false;
    private FrameLayout visualTutorialOverlayView;
    private View visualItemTutorialTarget;
    private boolean visualItemTutorialScheduled = false;
    private boolean visualItemTutorialRunning = false;
    private static final long PROFILE_REFRESH_COOLDOWN_MS = 60L * 1000L;
    private static final long PROFILE_SEARCH_COOLDOWN_MS = 20L * 1000L;
    private static final long FAVORITES_REFRESH_COOLDOWN_MS = 15L * 1000L;
    private long lastProfileSearchStartedAt = 0L;
    private ScrollView mainScroll;
    private LinearLayout pullRefreshChip;
    private CircularPullProgressView pullRefreshSpinner;
    private TextView pullRefreshText;
    private long lastSameNickRefreshAt = 0L;
    private float pullStartY = 0f;
    private boolean pullStartedAtTop = false;
    private boolean pullReadyToRefresh = false;
    private boolean pullDragging = false;
    private final ArrayList<ProfileHistoryItem> openedProfilesHistory = new ArrayList<>();
    private final ArrayList<ProfileHistoryItem> favoriteProfiles = new ArrayList<>();
    private String currentHotelKey = "br";

    private InterstitialAd interstitialAd;
    private boolean interstitialLoading = false;
    private boolean interstitialShowing = false;
    private boolean mobileAdsInitialized = false;
    private long lastInterstitialShownAt = 0L;
    private int profileOpenActionsSinceAd = 0;
    private boolean pendingProfileInterstitialAction = false;
    private long pendingProfileInterstitialRequestedAt = 0L;
    private static final long PROFILE_INTERSTITIAL_PENDING_WINDOW_MS = 5L * 60L * 1000L;
    private int interstitialLoadFailureCount = 0;
    private long nextInterstitialLoadAllowedAt = 0L;
    private Runnable interstitialRetryRunnable = null;
    private static final String REAL_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8079226281001828/5039255014";
    private static final String TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String REAL_REWARDED_AD_UNIT_ID = "ca-app-pub-8079226281001828/1283312609";
    private static final String TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    private static final String REAL_START_NATIVE_AD_UNIT_ID = "ca-app-pub-8079226281001828/4100478754";
    private static final String TEST_START_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110";
    private static final String REAL_PREVIOUS_STYLES_BANNER_AD_UNIT_ID = "ca-app-pub-8079226281001828/1381533840";
    private static final String REAL_FRIENDS_REMOVED_BANNER_AD_UNIT_ID = "ca-app-pub-8079226281001828/5249048126";
    private static final String REAL_VISUAL_COLORS_BANNER_AD_UNIT_ID = "ca-app-pub-8079226281001828/6444755891";
    private static final String REAL_VISUAL_NICK_SEARCH_BANNER_AD_UNIT_ID = "ca-app-pub-8079226281001828/9823552100";
    private static final String TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741";
    private static final boolean USE_TEST_ADS = false;
    private static final String ADS_LOG_TAG = "ToxicAds";
    private static final String INTERSTITIAL_AD_UNIT_ID = TEST_INTERSTITIAL_AD_UNIT_ID; // diagnostic: only interstitial uses Google test ID
    private static final String REWARDED_AD_UNIT_ID = USE_TEST_ADS ? TEST_REWARDED_AD_UNIT_ID : REAL_REWARDED_AD_UNIT_ID;
    private static final String START_NATIVE_AD_UNIT_ID = USE_TEST_ADS ? TEST_START_NATIVE_AD_UNIT_ID : REAL_START_NATIVE_AD_UNIT_ID;
    private static final String PREVIOUS_STYLES_BANNER_AD_UNIT_ID = USE_TEST_ADS ? TEST_BANNER_AD_UNIT_ID : REAL_PREVIOUS_STYLES_BANNER_AD_UNIT_ID;
    private static final String FRIENDS_REMOVED_BANNER_AD_UNIT_ID = USE_TEST_ADS ? TEST_BANNER_AD_UNIT_ID : REAL_FRIENDS_REMOVED_BANNER_AD_UNIT_ID;
    private static final String VISUAL_COLORS_BANNER_AD_UNIT_ID = USE_TEST_ADS ? TEST_BANNER_AD_UNIT_ID : REAL_VISUAL_COLORS_BANNER_AD_UNIT_ID;
    private static final String VISUAL_NICK_SEARCH_BANNER_AD_UNIT_ID = USE_TEST_ADS ? TEST_BANNER_AD_UNIT_ID : REAL_VISUAL_NICK_SEARCH_BANNER_AD_UNIT_ID;
    private AdView previousStylesBannerAdView;
    private FrameLayout previousStylesBannerAdContainer;
    private boolean previousStylesBannerLoadStarted = false;
    private AdView friendsRemovedBannerAdView;
    private FrameLayout friendsRemovedBannerAdContainer;
    private boolean friendsRemovedBannerLoadStarted = false;
    private AdView visualColorsBannerAdView;
    private FrameLayout visualColorsBannerAdContainer;
    private boolean visualColorsBannerLoadStarted = false;
    private AdView visualNickSearchBannerAdView;
    private FrameLayout visualNickSearchBannerAdContainer;
    private boolean visualNickSearchBannerLoadStarted = false;
    private static final long BANNER_RETRY_BASE_DELAY_MS = 30L * 1000L;
    private static final long BANNER_RETRY_MAX_DELAY_MS = 5L * 60L * 1000L;
    private static final int BANNER_RETRY_MAX_SHIFT = 4;
    private final Map<AdView, Integer> bannerLoadFailureCounts = new IdentityHashMap<>();
    private final Map<AdView, Runnable> bannerRetryRunnables = new IdentityHashMap<>();
    private final Set<AdView> bannerHasLoadedAds = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final long INTERSTITIAL_COOLDOWN_MS = 120L * 1000L;
    private static final int ACTIONS_BETWEEN_INTERSTITIALS = 1;
    private static final long AD_RETRY_BASE_DELAY_MS = 15L * 1000L;
    private static final long AD_RETRY_MAX_DELAY_MS = 2L * 60L * 1000L;
    private static final int AD_RETRY_MAX_SHIFT = 4;
    private RewardedAd rewardedAd;
    private boolean rewardedLoading = false;
    private int rewardedLoadFailureCount = 0;
    private long nextRewardedLoadAllowedAt = 0L;
    private Runnable rewardedRetryRunnable = null;
    private TextView rewardAdBtn;
    private TextView rewardAdTimeLabel;
    private ImageView selectedHotelFlag;
    private LinearLayout sponsorsSection;
    private FrameLayout sponsorsCarouselHost;
    private HorizontalScrollView sponsorsCarouselScroll;
    private LinearLayout sponsorsCarouselRow;
    private ProgressBar sponsorsLoadingIndicator;
    private View sponsorsSubscribeButton;
    private TextView sponsorsActionIcon;
    private View sponsorsActionGlow;
    private static final long SPONSOR_GLOW_CYCLE_MS = 7_000L;
    private FrameLayout startNativeAdContainer;
    private NativeAd startNativeAd;
    private boolean startNativeAdLoading = false;
    private long startNativeAdRetryAfterMs = 0L;
    private boolean startScreenVisible = true;
    private volatile boolean sponsorsLoading = false;
    private volatile String sponsorsCacheJson = null;
    private static final String SUPPORTER_PRODUCT_ID = "tx_supporter";
    private static final String SUPPORTER_BASE_PLAN_ID = "basic";
    private static final String PREF_SUPPORTER_TUTORIAL_PENDING = "supporter_tutorial_pending";
    private static final String PREF_SUPPORTER_TUTORIAL_VERSION = "supporter_tutorial_version";
    private static final int CURRENT_SUPPORTER_TUTORIAL_VERSION = 2;
    private static final long SUPPORTER_REVERIFY_INTERVAL_MS = 15L * 60L * 1000L;
    private ProductDetails supporterProductDetails;
    private boolean supporterActive = false;
    private boolean supporterStatusRequestRunning = false;
    private boolean supporterPurchaseQueryRunning = false;
    private boolean supporterProductDetailsQueryRunning = false;
    private boolean billingEntitlementCheckPending = true;
    private boolean pendingSupporterPurchaseLaunch = false;
    private String supporterPurchaseToken = "";
    private long supporterCanChangeAtMs = 0L;
    private long supporterExpiresAtMs = 0L;
    private long supporterNextVerificationAtMs = 0L;
    private String supporterProfileNick = "";
    private String supporterProfileHotel = "";
    private Runnable billingEntitlementTimeoutRunnable;
    private boolean openingSplashShownThisSession = false;
    private JSONObject visualFigureDataCache = null;
    private long visualFigureDataLoadedAt = 0L;
    private static final String VISUAL_FIGUREDATA_URL = "https://atoxic.com.br/tools/converter_figuredata.php?json=1";
    private static final String VISUAL_FIGUREDATA_CACHE_URL = "https://atoxic.com.br/cache/figuredata-ui.json";
    private static final String VISUAL_FIGUREDATA_DISK_CACHE_FILE = "visual_figuredata_cache.json";
    private static final long VISUAL_FIGUREDATA_CACHE_TTL_MS = 24L * 60L * 60L * 1000L;
    private static final String[] VISUAL_PRELOAD_TYPES = new String[] {
            "hd", "hr", "ha", "he", "ea", "fa",
            "ch", "ca", "cc", "cp",
            "lg", "sh", "wa",
            "pt", "mc"
    };
    private static final String DEFAULT_VISUAL_FIGURE_MALE = "hd-180-22-0";
    private static final String DEFAULT_VISUAL_FIGURE_FEMALE = "hd-600-1-0";
    private static final String DEFAULT_VISUAL_FIGURE = DEFAULT_VISUAL_FIGURE_MALE;
    private long adFreeUntilMs = 0L;
    private final Runnable adFreeTicker = new Runnable() {
        @Override public void run() {
            refreshSupporterEntitlementIfNeeded();
            consumeAdFreeElapsed();
            updateRewardButtonText();
            if (!removeAdsPurchased && !hasAdFreeAccess()) {
                preloadBannerAds();
                loadInterstitialAd();
                loadStartNativeAdIfNeeded();
            }
            updateStartNativeAdVisibility();
            uiHandler.postDelayed(this, 1000L);
        }
    };
    private static final String PREF_AD_FREE_UNTIL_MS = "ad_free_until_ms";
    private static final String PREF_REWARDED_ADS_WATCHED = "rewarded_ads_watched";
    private static final String PREF_REMOVE_ADS_PURCHASED = "remove_ads_purchased";
    private static final String REMOVE_ADS_PRODUCT_ID = "remove_ads";
    private boolean removeAdsPurchased = false;
    private BillingClient billingClient;
    private ProductDetails removeAdsProductDetails;
    private boolean removeAdsProductDetailsQueryRunning = false;
    private boolean billingConnecting = false;
    private boolean billingReady = false;
    private boolean pendingRemoveAdsPurchaseLaunch = false;
    private static final long REWARDED_AD_FREE_MS = 2L * 60L * 60L * 1000L;
    private static final long MAX_AD_FREE_MS = 4L * 60L * 60L * 1000L;
    private static final int REWARDED_ADS_REQUIRED = 3;
    private int rewardedAdsWatched = 0;
    private long lastFavoritesPullRefreshAt = 0L;
    private boolean appInForeground = true;
    private static final long FAVORITE_ONLINE_FOREGROUND_INTERVAL_MS = 15L * 1000L;
    private static final long FAVORITE_ONLINE_BACKGROUND_INTERVAL_MS = 60L * 1000L;
    private static final long ACCESS_GATE_BLOCKED_RECHECK_MS = 3L * 1000L;
    private static final long ACCESS_GATE_CLEAR_RECHECK_MS = 60L * 1000L;
    private static final int ACCESS_GATE_CONNECT_TIMEOUT_MS = 3500;
    private static final int ACCESS_GATE_READ_TIMEOUT_MS = 3500;
    private static final String[] ACCESS_GATE_CONTROL_URLS = new String[] {
            "https://atoxic.com.br/",
            "https://www.habbo.com/"
    };
    private static final String[][] ACCESS_GATE_AD_PROBES = new String[][] {
            {
                    "pagead2.googlesyndication.com",
                    "https://pagead2.googlesyndication.com/pagead/gen_204?id=gmob-apps"
            },
            {
                    "googleads.g.doubleclick.net",
                    "https://googleads.g.doubleclick.net/pagead/gen_204?id=gmob-apps"
            }
    };
    private ConnectivityManager accessConnectivityManager;
    private ConnectivityManager.NetworkCallback accessNetworkCallback;
    private boolean accessNetworkCallbackRegistered = false;
    private boolean accessProbeRunning = false;
    private boolean accessProbeRerunRequested = false;
    private int accessProbeGeneration = 0;
    private Dialog accessGateDialog;
    private AccessGateReason accessGateReason = AccessGateReason.NONE;
    private final Runnable accessGateRecheckRunnable = this::requestAccessGateCheck;

    private final int bg = Color.rgb(8, 9, 14);
    private final int purple = Color.rgb(139, 92, 246);
    private final int purple2 = Color.rgb(91, 33, 182);
    private final int pink = Color.rgb(192, 132, 252);
    private final int blue = Color.rgb(56, 189, 248);
    private final int green = Color.rgb(52, 211, 153);
    private final int red = Color.rgb(248, 113, 113);
    private final int cardFill = Color.rgb(22, 20, 30);
    private final int cardStroke = Color.rgb(54, 49, 70);
    private final int muted = Color.rgb(176, 171, 193);
    private Typeface habboFont;
    private boolean lightTheme = false;
    private boolean notifyFavoriteOnline = true;
    private final ConcurrentHashMap<String, Boolean> favoriteOnlineStates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FavoriteStatus> favoriteStatusCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> favoriteOnlineLastToast = new ConcurrentHashMap<>();
    private final ArrayList<TextView> favoriteOnlineBadgeViews = new ArrayList<>();
    private Runnable favoriteOnlineWatcher = null;
    private String visualEditorCachedFigure = DEFAULT_VISUAL_FIGURE;
    private String visualEditorCachedGender = "M";
    private String visualEditorCachedType = "hd";
    private int visualEditorCachedDirection = 2;
    private String loadingProfileFigureHint = "";
    private String loadingProfileUniqueIdHint = "";
    private String loadingProfileHotelHint = "";
    private ImageView loadingProfileAvatarImage = null;
    private final Map<String, View> visualItemViewsSessionCache = new HashMap<>();
    private final Map<String, Integer> visualItemRenderLimits = new HashMap<>();
    private final Set<String> visualCategoryLoading = Collections.synchronizedSet(new HashSet<>());

    private enum AccessGateReason {
        NONE,
        OFFLINE,
        AD_BLOCKER
    }

    private static class AccessProbeResult {
        final boolean appInternetReachable;
        final boolean adServicesReachable;

        AccessProbeResult(boolean appInternetReachable, boolean adServicesReachable) {
            this.appInternetReachable = appInternetReachable;
            this.adServicesReachable = adServicesReachable;
        }
    }

    private interface IntChangeListener {
        void onChange(int value);
    }

    private interface PullTouchListener {
        void onPullTouch(MotionEvent event);
    }

    private class PullDispatchFrameLayout extends FrameLayout {
        private PullTouchListener pullTouchListener;

        PullDispatchFrameLayout(Context context) {
            super(context);
        }

        void setPullTouchListener(PullTouchListener listener) {
            this.pullTouchListener = listener;
        }

        @Override public boolean dispatchTouchEvent(MotionEvent event) {
            if (pullTouchListener != null) pullTouchListener.onPullTouch(event);
            return super.dispatchTouchEvent(event);
        }
    }

    @Override public void onCreate(Bundle b) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(b);
        try {
            if (getActionBar() != null) getActionBar().hide();
        } catch (Exception ignored) {}
        lightTheme = getSharedPreferences(PREFS, MODE_PRIVATE).getString("theme", "dark").equals("light");
        currentHotelKey = normalizeHotelKey(getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_HOTEL, ""));
        if (currentHotelKey.isEmpty()) {
            currentHotelKey = defaultHotelForDeviceLocale();
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_HOTEL, currentHotelKey).apply();
        }
        clearLegacyApiProfileCache();
        try {
            habboFont = Typeface.createFromAsset(getAssets(), "fonts/ubuntu_habbo.ttf");
        } catch (Exception e) {
            habboFont = Typeface.create("sans-serif-condensed", Typeface.BOLD);
        }
        getWindow().setStatusBarColor(lightTheme ? Color.WHITE : bg);
        getWindow().setNavigationBarColor(lightTheme ? Color.rgb(245, 245, 245) : bg);
        if (Build.VERSION.SDK_INT >= 29) {
            getWindow().setNavigationBarContrastEnforced(false);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            int flags = lightTheme ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0;
            if (Build.VERSION.SDK_INT >= 26 && lightTheme) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        loadOpenedProfilesHistory();
        loadFavoriteProfiles();
        loadFavoriteOnlineStatesFromPrefs();
        notifyFavoriteOnline = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PREF_NOTIFY_FAVORITE_ONLINE, true);
        loadVisualEditorState();
        adFreeUntilMs = getSharedPreferences(PREFS, MODE_PRIVATE).getLong(PREF_AD_FREE_UNTIL_MS, 0L);
        rewardedAdsWatched = Math.max(0, Math.min(
                REWARDED_ADS_REQUIRED - 1,
                getSharedPreferences(PREFS, MODE_PRIVATE).getInt(PREF_REWARDED_ADS_WATCHED, 0)
        ));
        removeAdsPurchased = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PREF_REMOVE_ADS_PURCHASED, false);
        MobileAds.initialize(this, initializationStatus -> {
            android.util.Log.i(ADS_LOG_TAG, "MobileAds initialized");
            runOnUiThread(() -> {
                mobileAdsInitialized = true;
                if (!billingEntitlementCheckPending && !hasConfirmedAdFreeAccess()) {
                    preloadBannerAds();
                    loadInterstitialAd();
                    refreshAttachedProfileBannerAds();
                    maybeShowPendingProfileInterstitial();
                }
            });
        });
        buildUi();
        startAccessGateMonitoring();
        initBillingClient();
        billingEntitlementTimeoutRunnable = () -> finishBillingEntitlementCheck(false);
        uiHandler.postDelayed(billingEntitlementTimeoutRunnable, 6500L);
        refreshSponsors();
        requestFavoriteNotificationPermissionIfNeeded();
        startFavoriteOnlineWatcher();
        updateFavoriteOnlineAlarm();
    }
    private void requestFavoriteNotificationPermissionIfNeeded() {
        try {
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 2606);
            }
        } catch(Exception ignored) {}
    }

    private void applySystemBarsForTheme() {
        getWindow().setStatusBarColor(lightTheme ? Color.WHITE : bg);
        getWindow().setNavigationBarColor(lightTheme ? Color.WHITE : bg);
        if (Build.VERSION.SDK_INT >= 29) {
            getWindow().setNavigationBarContrastEnforced(false);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            int flags = lightTheme ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0;
            if (Build.VERSION.SDK_INT >= 26 && lightTheme) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        if (screen != null) applySafeAreaInsets(getWindow(), screen);
    }

    private void applySafeAreaInsets(Window window, View content) {
        if (window == null || content == null || Build.VERSION.SDK_INT < 30) return;

        try {
            window.setDecorFitsSystemWindows(false);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        } catch (Exception ignored) {}

        int[] basePadding = safeAreaPaddingByView.get(content);
        if (basePadding == null) {
            basePadding = new int[] {
                    content.getPaddingLeft(),
                    content.getPaddingTop(),
                    content.getPaddingRight(),
                    content.getPaddingBottom()
            };
            safeAreaPaddingByView.put(content, basePadding);
            final int[] stableBasePadding = basePadding;
            content.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets safe = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout()
                );
                view.setPadding(
                        stableBasePadding[0] + safe.left,
                        stableBasePadding[1] + safe.top,
                        stableBasePadding[2] + safe.right,
                        stableBasePadding[3] + safe.bottom
                );
                return insets;
            });
        }

        content.post(content::requestApplyInsets);
    }

    private long calculateAdRetryDelayMs(int failureCount) {
        int safeFailureCount = Math.max(1, failureCount);
        int shift = Math.min(AD_RETRY_MAX_SHIFT, safeFailureCount - 1);
        long delay = AD_RETRY_BASE_DELAY_MS << shift;
        return Math.min(delay, AD_RETRY_MAX_DELAY_MS);
    }

    private void cancelInterstitialAdRetry() {
        if (interstitialRetryRunnable != null) {
            uiHandler.removeCallbacks(interstitialRetryRunnable);
            interstitialRetryRunnable = null;
        }
    }

    private void cancelRewardedAdRetry() {
        if (rewardedRetryRunnable != null) {
            uiHandler.removeCallbacks(rewardedRetryRunnable);
            rewardedRetryRunnable = null;
        }
    }

    private void resetInterstitialBackoff() {
        interstitialLoadFailureCount = 0;
        nextInterstitialLoadAllowedAt = 0L;
        cancelInterstitialAdRetry();
    }

    private void resetRewardedBackoff() {
        rewardedLoadFailureCount = 0;
        nextRewardedLoadAllowedAt = 0L;
        cancelRewardedAdRetry();
    }

    private boolean isCurrentBannerAdView(AdView adView) {
        return adView != null && (
                adView == previousStylesBannerAdView
                        || adView == friendsRemovedBannerAdView
                        || adView == visualColorsBannerAdView
                        || adView == visualNickSearchBannerAdView
        );
    }

    private void setBannerLoadStarted(AdView adView, boolean started) {
        if (adView == previousStylesBannerAdView) previousStylesBannerLoadStarted = started;
        else if (adView == friendsRemovedBannerAdView) friendsRemovedBannerLoadStarted = started;
        else if (adView == visualColorsBannerAdView) visualColorsBannerLoadStarted = started;
        else if (adView == visualNickSearchBannerAdView) visualNickSearchBannerLoadStarted = started;
    }

    private long calculateBannerRetryDelayMs(int failureCount) {
        int safeFailureCount = Math.max(1, failureCount);
        int shift = Math.min(BANNER_RETRY_MAX_SHIFT, safeFailureCount - 1);
        long delay = BANNER_RETRY_BASE_DELAY_MS << shift;
        return Math.min(delay, BANNER_RETRY_MAX_DELAY_MS);
    }

    private void cancelBannerAdRetry(AdView adView) {
        if (adView == null) return;
        Runnable retry = bannerRetryRunnables.remove(adView);
        if (retry != null) uiHandler.removeCallbacks(retry);
    }

    private void cancelAllBannerAdRetries() {
        for (Runnable retry : new ArrayList<>(bannerRetryRunnables.values())) {
            if (retry != null) uiHandler.removeCallbacks(retry);
        }
        bannerRetryRunnables.clear();
    }

    private void scheduleBannerAdRetry(final AdView adView, final FrameLayout container) {
        if (adView == null || container == null || !isCurrentBannerAdView(adView)) return;
        cancelBannerAdRetry(adView);
        int failureCount = bannerLoadFailureCounts.containsKey(adView)
                ? bannerLoadFailureCounts.get(adView) + 1
                : 1;
        bannerLoadFailureCounts.put(adView, failureCount);
        if (!appInForeground || removeAdsPurchased || hasAdFreeAccess()) return;

        Runnable retry = () -> {
            bannerRetryRunnables.remove(adView);
            if (!appInForeground
                    || removeAdsPurchased
                    || hasAdFreeAccess()
                    || !isCurrentBannerAdView(adView)
                    || container.getParent() == null) {
                return;
            }
            requestBannerLoadForContainer(container);
        };
        bannerRetryRunnables.put(adView, retry);
        long delay = calculateBannerRetryDelayMs(failureCount);
        if (adView == previousStylesBannerAdView || adView == friendsRemovedBannerAdView) {
            delay = Math.min(delay, 12L * 1000L);
        }
        uiHandler.postDelayed(retry, delay);
    }

    private void handleBannerLoadFailure(AdView adView, FrameLayout container) {
        if (!isCurrentBannerAdView(adView)) return;
        setBannerLoadStarted(adView, false);
        if (container != null) {
            container.setVisibility(bannerHasLoadedAds.contains(adView) ? View.VISIBLE : View.GONE);
        }
        scheduleBannerAdRetry(adView, container);
    }

    private void detachViewFromParent(View view) {
        try {
            if (view == null) return;
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) ((ViewGroup) parent).removeView(view);
        } catch (Exception ignored) {}
    }

    private FrameLayout newBannerContainer() {
        FrameLayout container = new FrameLayout(this);
        container.setPadding(0, dp(6), 0, dp(6));
        // Start as INVISIBLE instead of GONE so the banner can be measured when attached.
        // It becomes VISIBLE only after AdMob confirms an ad, and GONE if there is no fill/error.
        container.setVisibility(View.INVISIBLE);
        return container;
    }

    private AdView newBannerAdView(String adUnitId, FrameLayout container) {
        final AdView adView = new AdView(this);
        adView.setAdUnitId(adUnitId);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                android.util.Log.i(ADS_LOG_TAG, "Banner loaded: " + adView.getAdUnitId());
                cancelBannerAdRetry(adView);
                bannerLoadFailureCounts.remove(adView);
                bannerHasLoadedAds.add(adView);
                if (container != null && !hasAdFreeAccess()) {
                    container.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                android.util.Log.w(ADS_LOG_TAG,
                        "Banner failed: " + adView.getAdUnitId()
                                + " code=" + (loadAdError == null ? -1 : loadAdError.getCode())
                                + " domain=" + (loadAdError == null ? "" : loadAdError.getDomain())
                                + " message=" + (loadAdError == null ? "" : loadAdError.getMessage()));
                handleBannerLoadFailure(adView, container);
            }

            @Override
            public void onAdClosed() {
                if (container != null
                        && !hasAdFreeAccess()
                        && isCurrentBannerAdView(adView)
                        && bannerHasLoadedAds.contains(adView)) {
                    container.setVisibility(View.VISIBLE);
                }
            }
        });
        container.addView(adView, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        return adView;
    }

    private void loadBannerAfterAttach(final AdView adView, final FrameLayout container) {
        if (adView == null || container == null || removeAdsPurchased || hasAdFreeAccess()) return;
        container.setVisibility(View.INVISIBLE);
        container.post(() -> {
            try {
                if (removeAdsPurchased || hasAdFreeAccess()) {
                    container.setVisibility(View.GONE);
                    setBannerLoadStarted(adView, false);
                    return;
                }
                if (container.getParent() == null) {
                    setBannerLoadStarted(adView, false);
                    return;
                }
                int widthPx = container.getWidth();
                if (widthPx <= 0) widthPx = getResources().getDisplayMetrics().widthPixels - dp(36);
                int adWidthDp = Math.max(1, (int) (widthPx / getResources().getDisplayMetrics().density));
                adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidthDp));
                adView.loadAd(new AdRequest.Builder().build());
            } catch (Exception ignored) {
                handleBannerLoadFailure(adView, container);
            }
        });
    }

    private void requestPreviousStylesBannerLoadIfNeeded() {
        if (billingEntitlementCheckPending || hasConfirmedAdFreeAccess()) return;
        if (previousStylesBannerLoadStarted || previousStylesBannerAdView == null || previousStylesBannerAdContainer == null) return;
        previousStylesBannerLoadStarted = true;
        loadBannerAfterAttach(previousStylesBannerAdView, previousStylesBannerAdContainer);
    }

    private void requestFriendsRemovedBannerLoadIfNeeded() {
        if (billingEntitlementCheckPending || hasConfirmedAdFreeAccess()) return;
        if (friendsRemovedBannerLoadStarted || friendsRemovedBannerAdView == null || friendsRemovedBannerAdContainer == null) return;
        friendsRemovedBannerLoadStarted = true;
        loadBannerAfterAttach(friendsRemovedBannerAdView, friendsRemovedBannerAdContainer);
    }

    private void requestVisualColorsBannerLoadIfNeeded() {
        if (billingEntitlementCheckPending || hasConfirmedAdFreeAccess()) return;
        if (visualColorsBannerLoadStarted || visualColorsBannerAdView == null || visualColorsBannerAdContainer == null) return;
        visualColorsBannerLoadStarted = true;
        loadBannerAfterAttach(visualColorsBannerAdView, visualColorsBannerAdContainer);
    }

    private void requestVisualNickSearchBannerLoadIfNeeded() {
        if (billingEntitlementCheckPending || hasConfirmedAdFreeAccess()) return;
        if (visualNickSearchBannerLoadStarted || visualNickSearchBannerAdView == null || visualNickSearchBannerAdContainer == null) return;
        visualNickSearchBannerLoadStarted = true;
        loadBannerAfterAttach(visualNickSearchBannerAdView, visualNickSearchBannerAdContainer);
    }

    private void requestBannerLoadForContainer(View banner) {
        if (banner == previousStylesBannerAdContainer) requestPreviousStylesBannerLoadIfNeeded();
        else if (banner == friendsRemovedBannerAdContainer) requestFriendsRemovedBannerLoadIfNeeded();
        else if (banner == visualColorsBannerAdContainer) requestVisualColorsBannerLoadIfNeeded();
        else if (banner == visualNickSearchBannerAdContainer) requestVisualNickSearchBannerLoadIfNeeded();
    }

    private void ensurePreviousStylesBannerAd() {
        // During the short billing check we may already render a very fast profile.
        // Prepare the invisible slot now; loading/display waits for entitlement confirmation.
        if (hasConfirmedAdFreeAccess()) return;
        if (previousStylesBannerAdContainer == null || previousStylesBannerAdView == null) {
            previousStylesBannerAdContainer = newBannerContainer();
            previousStylesBannerAdView = newBannerAdView(PREVIOUS_STYLES_BANNER_AD_UNIT_ID, previousStylesBannerAdContainer);
            previousStylesBannerLoadStarted = false;
        }
    }

    private void ensureFriendsRemovedBannerAd() {
        // During the short billing check we may already render a very fast profile.
        // Prepare the invisible slot now; loading/display waits for entitlement confirmation.
        if (hasConfirmedAdFreeAccess()) return;
        if (friendsRemovedBannerAdContainer == null || friendsRemovedBannerAdView == null) {
            friendsRemovedBannerAdContainer = newBannerContainer();
            friendsRemovedBannerAdView = newBannerAdView(FRIENDS_REMOVED_BANNER_AD_UNIT_ID, friendsRemovedBannerAdContainer);
            friendsRemovedBannerLoadStarted = false;
        }
    }

    private void ensureVisualColorsBannerAd() {
        // During the short billing check we may already render a very fast profile.
        // Prepare the invisible slot now; loading/display waits for entitlement confirmation.
        if (hasConfirmedAdFreeAccess()) return;
        if (visualColorsBannerAdContainer == null || visualColorsBannerAdView == null) {
            visualColorsBannerAdContainer = newBannerContainer();
            visualColorsBannerAdView = newBannerAdView(VISUAL_COLORS_BANNER_AD_UNIT_ID, visualColorsBannerAdContainer);
            visualColorsBannerLoadStarted = false;
        }
    }

    private void ensureVisualNickSearchBannerAd() {
        // During the short billing check we may already render a very fast profile.
        // Prepare the invisible slot now; loading/display waits for entitlement confirmation.
        if (hasConfirmedAdFreeAccess()) return;
        if (visualNickSearchBannerAdContainer == null || visualNickSearchBannerAdView == null) {
            visualNickSearchBannerAdContainer = newBannerContainer();
            visualNickSearchBannerAdView = newBannerAdView(VISUAL_NICK_SEARCH_BANNER_AD_UNIT_ID, visualNickSearchBannerAdContainer);
            visualNickSearchBannerLoadStarted = false;
        }
    }

    private View buildPreviousStylesBannerAd() {
        ensurePreviousStylesBannerAd();
        if (previousStylesBannerAdContainer == null) return null;
        detachViewFromParent(previousStylesBannerAdContainer);
        return previousStylesBannerAdContainer;
    }

    private View buildFriendsRemovedBannerAd() {
        ensureFriendsRemovedBannerAd();
        if (friendsRemovedBannerAdContainer == null) return null;
        detachViewFromParent(friendsRemovedBannerAdContainer);
        return friendsRemovedBannerAdContainer;
    }

    private View buildVisualColorsBannerAd() {
        ensureVisualColorsBannerAd();
        if (visualColorsBannerAdContainer == null) return null;
        detachViewFromParent(visualColorsBannerAdContainer);
        return visualColorsBannerAdContainer;
    }

    private View buildVisualNickSearchBannerAd() {
        ensureVisualNickSearchBannerAd();
        if (visualNickSearchBannerAdContainer == null) return null;
        detachViewFromParent(visualNickSearchBannerAdContainer);
        return visualNickSearchBannerAdContainer;
    }

    private void addBannerToResultWrap(View banner, int bottomMarginDp) {
        if (banner == null || resultWrap == null) return;
        detachViewFromParent(banner);
        resultWrap.addView(banner, lp(-1, dp(68), 0, 0, 0, bottomMarginDp));
        if (banner instanceof FrameLayout) {
            FrameLayout slot = (FrameLayout) banner;
            // Profile banners are often attached before the Play entitlement check finishes.
            // Always retry shortly after attachment so a slot cannot remain permanently INVISIBLE.
            slot.post(() -> requestBannerLoadForContainer(slot));
            slot.postDelayed(() -> {
                if (!billingEntitlementCheckPending && !hasConfirmedAdFreeAccess() && slot.getParent() != null) {
                    requestBannerLoadForContainer(slot);
                }
            }, 700L);
        } else {
            requestBannerLoadForContainer(banner);
        }
    }

    private void refreshAttachedProfileBannerAds() {
        if (billingEntitlementCheckPending || hasConfirmedAdFreeAccess()) return;
        if (previousStylesBannerAdContainer != null && previousStylesBannerAdContainer.getParent() != null) {
            if (!bannerHasLoadedAds.contains(previousStylesBannerAdView)) previousStylesBannerLoadStarted = false;
            requestPreviousStylesBannerLoadIfNeeded();
        }
        if (friendsRemovedBannerAdContainer != null && friendsRemovedBannerAdContainer.getParent() != null) {
            if (!bannerHasLoadedAds.contains(friendsRemovedBannerAdView)) friendsRemovedBannerLoadStarted = false;
            requestFriendsRemovedBannerLoadIfNeeded();
        }
    }

    private void preloadBannerAds() {
        if (removeAdsPurchased || hasAdFreeAccess()) return;
        ensurePreviousStylesBannerAd();
        ensureFriendsRemovedBannerAd();
        ensureVisualColorsBannerAd();
        ensureVisualNickSearchBannerAd();
    }

    private void pauseBannerAds() {
        try { if (previousStylesBannerAdView != null) previousStylesBannerAdView.pause(); } catch(Exception ignored) {}
        try { if (friendsRemovedBannerAdView != null) friendsRemovedBannerAdView.pause(); } catch(Exception ignored) {}
        try { if (visualColorsBannerAdView != null) visualColorsBannerAdView.pause(); } catch(Exception ignored) {}
        try { if (visualNickSearchBannerAdView != null) visualNickSearchBannerAdView.pause(); } catch(Exception ignored) {}
    }

    private void resumeBannerAds() {
        try { if (previousStylesBannerAdView != null) previousStylesBannerAdView.resume(); } catch(Exception ignored) {}
        try { if (friendsRemovedBannerAdView != null) friendsRemovedBannerAdView.resume(); } catch(Exception ignored) {}
        try { if (visualColorsBannerAdView != null) visualColorsBannerAdView.resume(); } catch(Exception ignored) {}
        try { if (visualNickSearchBannerAdView != null) visualNickSearchBannerAdView.resume(); } catch(Exception ignored) {}
        if (previousStylesBannerAdContainer != null && previousStylesBannerAdContainer.getParent() != null) requestPreviousStylesBannerLoadIfNeeded();
        if (friendsRemovedBannerAdContainer != null && friendsRemovedBannerAdContainer.getParent() != null) requestFriendsRemovedBannerLoadIfNeeded();
        if (visualColorsBannerAdContainer != null && visualColorsBannerAdContainer.getParent() != null) requestVisualColorsBannerLoadIfNeeded();
        if (visualNickSearchBannerAdContainer != null && visualNickSearchBannerAdContainer.getParent() != null) requestVisualNickSearchBannerLoadIfNeeded();
    }

    private void destroyBannerAd(AdView adView, FrameLayout container) {
        cancelBannerAdRetry(adView);
        bannerLoadFailureCounts.remove(adView);
        bannerHasLoadedAds.remove(adView);
        try { if (adView != null) adView.destroy(); } catch(Exception ignored) {}
        detachViewFromParent(container);
    }

    private void destroyAllBannerAds() {
        cancelAllBannerAdRetries();
        destroyBannerAd(previousStylesBannerAdView, previousStylesBannerAdContainer);
        destroyBannerAd(friendsRemovedBannerAdView, friendsRemovedBannerAdContainer);
        destroyBannerAd(visualColorsBannerAdView, visualColorsBannerAdContainer);
        destroyBannerAd(visualNickSearchBannerAdView, visualNickSearchBannerAdContainer);
        previousStylesBannerAdView = null;
        previousStylesBannerAdContainer = null;
        previousStylesBannerLoadStarted = false;
        friendsRemovedBannerAdView = null;
        friendsRemovedBannerAdContainer = null;
        friendsRemovedBannerLoadStarted = false;
        visualColorsBannerAdView = null;
        visualColorsBannerAdContainer = null;
        visualColorsBannerLoadStarted = false;
        visualNickSearchBannerAdView = null;
        visualNickSearchBannerAdContainer = null;
        visualNickSearchBannerLoadStarted = false;
        destroyStartNativeAd();
    }

    private void destroyStartNativeAd() {
        try {
            if (startNativeAd != null) startNativeAd.destroy();
        } catch(Exception ignored) {}
        startNativeAd = null;
        startNativeAdLoading = false;
        if (startNativeAdContainer != null) {
            startNativeAdContainer.removeAllViews();
            startNativeAdContainer.setVisibility(View.GONE);
        }
    }

    private void updateStartNativeAdVisibility() {
        if (startNativeAdContainer == null) return;
        boolean visible = startScreenVisible
                && startNativeAd != null
                && !removeAdsPurchased
                && !hasAdFreeAccess();
        startNativeAdContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void loadStartNativeAdIfNeeded() {
        if (!startScreenVisible || removeAdsPurchased || hasAdFreeAccess()) {
            updateStartNativeAdVisibility();
            return;
        }
        if (startNativeAd != null) {
            renderStartNativeAd();
            return;
        }
        long now = System.currentTimeMillis();
        if (startNativeAdLoading || now < startNativeAdRetryAfterMs) return;
        startNativeAdLoading = true;
        try {
            new AdLoader.Builder(this, START_NATIVE_AD_UNIT_ID)
                    .forNativeAd(ad -> {
                        startNativeAdLoading = false;
                        startNativeAdRetryAfterMs = 0L;
                        if (removeAdsPurchased || hasAdFreeAccess()) {
                            try { ad.destroy(); } catch(Exception ignored) {}
                            updateStartNativeAdVisibility();
                            return;
                        }
                        try {
                            if (startNativeAd != null) startNativeAd.destroy();
                        } catch(Exception ignored) {}
                        startNativeAd = ad;
                        renderStartNativeAd();
                    })
                    .withAdListener(new AdListener() {
                        @Override public void onAdFailedToLoad(LoadAdError error) {
                            startNativeAdLoading = false;
                            startNativeAdRetryAfterMs = System.currentTimeMillis() + 2L * 60L * 1000L;
                            updateStartNativeAdVisibility();
                        }
                    })
                    .build()
                    .loadAd(new AdRequest.Builder().build());
        } catch(Exception ignored) {
            startNativeAdLoading = false;
            startNativeAdRetryAfterMs = System.currentTimeMillis() + 2L * 60L * 1000L;
            updateStartNativeAdVisibility();
        }
    }

    private void renderStartNativeAd() {
        if (startNativeAdContainer == null || startNativeAd == null) {
            updateStartNativeAdVisibility();
            return;
        }
        startNativeAdContainer.removeAllViews();
        startNativeAdContainer.addView(
                buildStartNativeAdView(startNativeAd),
                new FrameLayout.LayoutParams(-1, -2)
        );
        updateStartNativeAdVisibility();
    }

    private NativeAdView buildStartNativeAdView(NativeAd ad) {
        NativeAdView adView = new NativeAdView(this);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(13), dp(12), dp(13), dp(13));
        card.setBackground(round(
                lightTheme ? Color.WHITE : Color.rgb(20, 18, 28),
                dp(22),
                lightTheme ? Color.rgb(224, 216, 232) : Color.rgb(54, 46, 70),
                1
        ));
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(3));
        adView.addView(card, new FrameLayout.LayoutParams(-1, -2));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top, new LinearLayout.LayoutParams(-1, -2));

        TextView badge = text(t(R.string.ad_badge), 9, Color.WHITE, true);
        badge.setGravity(Gravity.CENTER);
        badge.setIncludeFontPadding(false);
        badge.setPadding(dp(8), 0, dp(8), 0);
        badge.setBackground(grad(dp(999), purple2, purple));
        top.addView(badge, new LinearLayout.LayoutParams(-2, dp(22)));

        TextView headline = text(ad.getHeadline(), 16, lightTheme ? Color.rgb(36, 31, 41) : Color.WHITE, true);
        headline.setMaxLines(2);
        headline.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams headlineLp = new LinearLayout.LayoutParams(0, -2, 1f);
        headlineLp.leftMargin = dp(9);
        top.addView(headline, headlineLp);
        adView.setHeadlineView(headline);

        MediaView media = new MediaView(this);
        media.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        media.setBackgroundColor(lightTheme ? Color.rgb(244, 241, 247) : Color.rgb(13, 12, 19));
        applyRoundedClip(media, dp(16));
        LinearLayout.LayoutParams mediaLp = new LinearLayout.LayoutParams(-1, dp(170));
        mediaLp.topMargin = dp(10);
        card.addView(media, mediaLp);
        adView.setMediaView(media);

        TextView body = text(
                ad.getBody() == null ? "" : ad.getBody(),
                13,
                lightTheme ? Color.rgb(89, 79, 97) : Color.argb(210,255,255,255),
                false
        );
        body.setMaxLines(2);
        body.setEllipsize(TextUtils.TruncateAt.END);
        body.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(-1, -2);
        bodyLp.topMargin = dp(9);
        card.addView(body, bodyLp);
        adView.setBodyView(body);
        body.setVisibility(ad.getBody() == null || ad.getBody().trim().isEmpty() ? View.GONE : View.VISIBLE);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams bottomLp = new LinearLayout.LayoutParams(-1, dp(48));
        bottomLp.topMargin = dp(10);
        card.addView(bottom, bottomLp);

        ImageView icon = new ImageView(this);
        icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        applyRoundedClip(icon, dp(11));
        bottom.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));
        adView.setIconView(icon);
        if (ad.getIcon() != null && ad.getIcon().getDrawable() != null) {
            icon.setImageDrawable(ad.getIcon().getDrawable());
        } else {
            icon.setVisibility(View.GONE);
        }

        TextView advertiser = text(
                ad.getAdvertiser() == null ? "" : ad.getAdvertiser(),
                12,
                lightTheme ? Color.rgb(91, 78, 101) : Color.argb(190,255,255,255),
                true
        );
        advertiser.setSingleLine(true);
        advertiser.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams advertiserLp = new LinearLayout.LayoutParams(0, -2, 1f);
        advertiserLp.leftMargin = icon.getVisibility() == View.GONE ? 0 : dp(9);
        advertiserLp.rightMargin = dp(9);
        bottom.addView(advertiser, advertiserLp);
        adView.setAdvertiserView(advertiser);
        advertiser.setVisibility(ad.getAdvertiser() == null || ad.getAdvertiser().trim().isEmpty() ? View.INVISIBLE : View.VISIBLE);

        TextView action = text(
                ad.getCallToAction() == null ? "" : ad.getCallToAction(),
                12,
                Color.WHITE,
                true
        );
        action.setGravity(Gravity.CENTER);
        action.setSingleLine(true);
        action.setPadding(dp(14), 0, dp(14), 0);
        action.setBackground(grad(dp(13), purple2, purple));
        bottom.addView(action, new LinearLayout.LayoutParams(-2, dp(42)));
        adView.setCallToActionView(action);
        action.setVisibility(ad.getCallToAction() == null || ad.getCallToAction().trim().isEmpty() ? View.GONE : View.VISIBLE);

        adView.setNativeAd(ad);
        return adView;
    }

    private void registerInterstitialLoadFailure() {
        interstitialLoadFailureCount++;
        nextInterstitialLoadAllowedAt = System.currentTimeMillis() + calculateAdRetryDelayMs(interstitialLoadFailureCount);
        scheduleInterstitialAdRetry();
    }

    private void registerRewardedLoadFailure() {
        rewardedLoadFailureCount++;
        nextRewardedLoadAllowedAt = System.currentTimeMillis() + calculateAdRetryDelayMs(rewardedLoadFailureCount);
        scheduleRewardedAdRetry();
    }

    private void scheduleInterstitialAdRetry() {
        if (removeAdsPurchased || hasAdFreeAccess() || !appInForeground || interstitialRetryRunnable != null) return;
        long delay = Math.max(0L, nextInterstitialLoadAllowedAt - System.currentTimeMillis());
        interstitialRetryRunnable = () -> {
            interstitialRetryRunnable = null;
            loadInterstitialAd();
        };
        uiHandler.postDelayed(interstitialRetryRunnable, delay);
    }

    private void scheduleRewardedAdRetry() {
        if (removeAdsPurchased || supporterActive || billingEntitlementCheckPending || !appInForeground || rewardedRetryRunnable != null) return;
        long delay = Math.max(0L, nextRewardedLoadAllowedAt - System.currentTimeMillis());
        rewardedRetryRunnable = () -> {
            rewardedRetryRunnable = null;
            loadRewardedAd();
        };
        uiHandler.postDelayed(rewardedRetryRunnable, delay);
    }

    private boolean canLoadInterstitialAdNow() {
        return !removeAdsPurchased && !hasAdFreeAccess() && System.currentTimeMillis() >= nextInterstitialLoadAllowedAt;
    }

    private boolean canLoadRewardedAdNow() {
        return !removeAdsPurchased
                && !supporterActive
                && !billingEntitlementCheckPending
                && System.currentTimeMillis() >= nextRewardedLoadAllowedAt;
    }

    private void loadInterstitialAd() {
        if (removeAdsPurchased || hasConfirmedAdFreeAccess()) {
            cancelInterstitialAdRetry();
            interstitialAd = null;
            interstitialLoading = false;
            interstitialShowing = false;
            return;
        }

        // Full-screen ads are loaded only after Mobile Ads initialization has completed.
        // Banner AdViews tolerate an early attach better, but InterstitialAd.load() should not race init.
        if (!mobileAdsInitialized) {
            if (interstitialRetryRunnable == null && appInForeground) {
                interstitialRetryRunnable = () -> {
                    interstitialRetryRunnable = null;
                    loadInterstitialAd();
                };
                uiHandler.postDelayed(interstitialRetryRunnable, 350L);
            }
            return;
        }

        if (interstitialShowing || interstitialLoading || interstitialAd != null) return;
        if (!canLoadInterstitialAdNow()) {
            scheduleInterstitialAdRetry();
            return;
        }

        interstitialLoading = true;
        android.util.Log.i(ADS_LOG_TAG, "Interstitial request: " + INTERSTITIAL_AD_UNIT_ID);
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(
                this,
                INTERSTITIAL_AD_UNIT_ID,
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        android.util.Log.i(ADS_LOG_TAG, "Interstitial loaded: " + INTERSTITIAL_AD_UNIT_ID);
                        interstitialLoading = false;
                        interstitialAd = ad;
                        resetInterstitialBackoff();
                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                android.util.Log.i(ADS_LOG_TAG, "Interstitial dismissed: " + INTERSTITIAL_AD_UNIT_ID);
                                interstitialShowing = false;
                                interstitialAd = null;
                                loadInterstitialAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                android.util.Log.w(ADS_LOG_TAG,
                                        "Interstitial show failed: " + INTERSTITIAL_AD_UNIT_ID
                                                + " code=" + (adError == null ? -1 : adError.getCode())
                                                + " domain=" + (adError == null ? "" : adError.getDomain())
                                                + " message=" + (adError == null ? "" : adError.getMessage()));
                                interstitialShowing = false;
                                interstitialAd = null;
                                // Crucial: do NOT start the 2-minute cooldown and do NOT discard
                                // the profile-open action when show() itself failed.
                                registerInterstitialLoadFailure();
                                if (pendingProfileInterstitialAction) {
                                    uiHandler.postDelayed(MainActivity.this::maybeShowPendingProfileInterstitial, 500L);
                                }
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                android.util.Log.i(ADS_LOG_TAG, "Interstitial shown: " + INTERSTITIAL_AD_UNIT_ID);
                                // Only a real impression/show starts the two-minute cooldown.
                                lastInterstitialShownAt = System.currentTimeMillis();
                                pendingProfileInterstitialAction = false;
                                pendingProfileInterstitialRequestedAt = 0L;
                                profileOpenActionsSinceAd = 0;
                                interstitialAd = null;
                            }
                        });
                        maybeShowPendingProfileInterstitial();
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        android.util.Log.w(ADS_LOG_TAG,
                                "Interstitial failed: " + INTERSTITIAL_AD_UNIT_ID
                                        + " code=" + (loadAdError == null ? -1 : loadAdError.getCode())
                                        + " domain=" + (loadAdError == null ? "" : loadAdError.getDomain())
                                        + " message=" + (loadAdError == null ? "" : loadAdError.getMessage()));
                        interstitialLoading = false;
                        interstitialAd = null;
                        registerInterstitialLoadFailure();
                        if (pendingProfileInterstitialAction) {
                            uiHandler.postDelayed(MainActivity.this::maybeShowPendingProfileInterstitial, 500L);
                        }
                    }
                }
        );
    }

    private void schedulePendingInterstitialAt(long whenMs) {
        long delay = Math.max(0L, whenMs - System.currentTimeMillis());
        uiHandler.postDelayed(() -> {
            if (!pendingProfileInterstitialAction) return;
            maybeShowPendingProfileInterstitial();
        }, delay + 50L);
    }

    private void maybeShowPendingProfileInterstitial() {
        if (!pendingProfileInterstitialAction || interstitialShowing) return;

        long now = System.currentTimeMillis();
        if (pendingProfileInterstitialRequestedAt <= 0L
                || now - pendingProfileInterstitialRequestedAt > PROFILE_INTERSTITIAL_PENDING_WINDOW_MS) {
            pendingProfileInterstitialAction = false;
            pendingProfileInterstitialRequestedAt = 0L;
            return;
        }
        if (hasConfirmedAdFreeAccess()) {
            pendingProfileInterstitialAction = false;
            pendingProfileInterstitialRequestedAt = 0L;
            cancelInterstitialAdRetry();
            return;
        }
        if (billingEntitlementCheckPending
                || accessGateReason != AccessGateReason.NONE
                || !appInForeground
                || isFinishing()) {
            return;
        }

        long cooldownRemaining = INTERSTITIAL_COOLDOWN_MS - (now - lastInterstitialShownAt);
        if (lastInterstitialShownAt > 0L && cooldownRemaining > 0L) {
            // Keep the eligible action pending instead of throwing it away.
            schedulePendingInterstitialAt(now + cooldownRemaining);
            return;
        }

        if (interstitialAd == null) {
            loadInterstitialAd();
            return;
        }

        try {
            interstitialShowing = true;
            android.util.Log.i(ADS_LOG_TAG, "Interstitial show requested: " + INTERSTITIAL_AD_UNIT_ID);
            // Do not clear pending state or start cooldown here. Those happen only in
            // onAdShowedFullScreenContent(), after Google confirms the ad is actually visible.
            interstitialAd.show(this);
        } catch (Exception showError) {
            android.util.Log.w(ADS_LOG_TAG, "Interstitial show exception: " + showError.getMessage());
            interstitialShowing = false;
            interstitialAd = null;
            registerInterstitialLoadFailure();
        }
    }

    private void maybeShowProfileInterstitial() {
        profileOpenActionsSinceAd++;

        long now = System.currentTimeMillis();
        boolean actionCountOk = profileOpenActionsSinceAd >= ACTIONS_BETWEEN_INTERSTITIALS;

        if (hasConfirmedAdFreeAccess()) {
            pendingProfileInterstitialAction = false;
            pendingProfileInterstitialRequestedAt = 0L;
            cancelInterstitialAdRetry();
            return;
        }
        if (!actionCountOk) return;
        if (lastInterstitialShownAt > 0L
                && now - lastInterstitialShownAt < INTERSTITIAL_COOLDOWN_MS) {
            return;
        }

        // The action becomes pending only when the 2-minute interval is already satisfied.
        // SDK initialization, entitlement checks and ad loading may finish afterwards without
        // losing this eligible profile-open trigger.
        pendingProfileInterstitialAction = true;
        pendingProfileInterstitialRequestedAt = now;

        if (!mobileAdsInitialized) {
            loadInterstitialAd();
            return;
        }

        maybeShowPendingProfileInterstitial();
    }


    private void loadRewardedAd() {
        if (removeAdsPurchased || supporterActive || billingEntitlementCheckPending) {
            cancelRewardedAdRetry();
            return;
        }
        if (rewardedLoading || rewardedAd != null) return;
        if (!canLoadRewardedAdNow()) {
            scheduleRewardedAdRetry();
            return;
        }

        rewardedLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(
                this,
                REWARDED_AD_UNIT_ID,
                adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd ad) {
                        rewardedLoading = false;
                        rewardedAd = ad;
                        resetRewardedBackoff();
                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                rewardedAd = null;
                                loadRewardedAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                rewardedAd = null;
                                registerRewardedLoadFailure();
                                toast(t(R.string.cannot_show_video));
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                rewardedAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        rewardedLoading = false;
                        rewardedAd = null;
                        registerRewardedLoadFailure();
                    }
                }
        );
    }


    private void logBillingResult(String operation, BillingResult billingResult) {
        if (billingResult == null) {
            android.util.Log.w("ToxicBilling", operation + ": null BillingResult");
            return;
        }
        android.util.Log.w(
                "ToxicBilling",
                operation
                        + ": code=" + billingResult.getResponseCode()
                        + ", message=" + billingResult.getDebugMessage()
        );
    }

    private void showBillingFailure(String operation, BillingResult billingResult) {
        logBillingResult(operation, billingResult);
        int responseCode = billingResult == null
                ? BillingClient.BillingResponseCode.ERROR
                : billingResult.getResponseCode();
        int messageId = responseCode == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE
                || responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE
                ? R.string.purchase_unavailable
                : R.string.purchase_error;
        runOnUiThread(() -> toast(t(messageId)));
    }

    private void initBillingClient() {
        try {
            billingClient = BillingClient.newBuilder(this)
                    .setListener(new PurchasesUpdatedListener() {
                        @Override public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                            int code = billingResult == null ? BillingClient.BillingResponseCode.ERROR : billingResult.getResponseCode();
                            if (code == BillingClient.BillingResponseCode.OK && purchases != null) {
                                handleRemoveAdsPurchases(purchases, true);
                                handleSupporterPurchases(purchases, true);
                            } else if (code != BillingClient.BillingResponseCode.USER_CANCELED) {
                                showBillingFailure("onPurchasesUpdated", billingResult);
                            }
                        }
                    })
                    .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                    .enableAutoServiceReconnection()
                    .build();
            ensureBillingReady();
        } catch(Exception e) {
            billingClient = null;
            billingReady = false;
            billingConnecting = false;
            boolean purchaseWasPending = pendingRemoveAdsPurchaseLaunch || pendingSupporterPurchaseLaunch;
            pendingRemoveAdsPurchaseLaunch = false;
            pendingSupporterPurchaseLaunch = false;
            android.util.Log.w("ToxicBilling", "initBillingClient exception", e);
            if (purchaseWasPending) showBillingFailure("initBillingClient", null);
        }
    }

    private void ensureBillingReady() {
        try {
            if (billingClient == null) {
                initBillingClient();
                return;
            }
            if (billingClient.isReady()) {
                billingReady = true;
                if (pendingRemoveAdsPurchaseLaunch && removeAdsProductDetails == null) {
                    queryRemoveAdsProductDetails();
                }
                if (pendingSupporterPurchaseLaunch && supporterProductDetails == null) {
                    querySupporterProductDetails();
                }
                return;
            }
            billingReady = false;
            if (billingConnecting) {
                return;
            }
            billingConnecting = true;
            billingClient.startConnection(new BillingClientStateListener() {
                @Override public void onBillingSetupFinished(BillingResult billingResult) {
                    billingConnecting = false;
                    billingReady = billingResult != null && billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK;
                    if (billingReady) {
                        queryRemoveAdsProductDetails();
                        querySupporterProductDetails();
                        queryRemoveAdsPurchases();
                        querySupporterPurchases();
                        if (pendingRemoveAdsPurchaseLaunch && removeAdsProductDetails != null) runOnUiThread(() -> launchRemoveAdsPurchase());
                        if (pendingSupporterPurchaseLaunch && supporterProductDetails != null) runOnUiThread(() -> launchSupporterPurchase());
                    } else {
                        boolean purchaseWasPending = pendingRemoveAdsPurchaseLaunch || pendingSupporterPurchaseLaunch;
                        pendingRemoveAdsPurchaseLaunch = false;
                        pendingSupporterPurchaseLaunch = false;
                        if (purchaseWasPending) showBillingFailure("onBillingSetupFinished", billingResult);
                    }
                }
                @Override public void onBillingServiceDisconnected() {
                    billingConnecting = false;
                    billingReady = false;
                }
            });
        } catch(Exception e) {
            billingConnecting = false;
            billingReady = false;
            boolean purchaseWasPending = pendingRemoveAdsPurchaseLaunch || pendingSupporterPurchaseLaunch;
            pendingRemoveAdsPurchaseLaunch = false;
            pendingSupporterPurchaseLaunch = false;
            android.util.Log.w("ToxicBilling", "startConnection exception", e);
            if (purchaseWasPending) showBillingFailure("startConnection", null);
        }
    }

    private void queryRemoveAdsProductDetails() {
        try {
            if (billingClient == null || !billingClient.isReady() || removeAdsProductDetailsQueryRunning) return;
            removeAdsProductDetailsQueryRunning = true;
            ArrayList<QueryProductDetailsParams.Product> products = new ArrayList<>();
            products.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(REMOVE_ADS_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
            QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                    .setProductList(products)
                    .build();
            billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
                @Override public void onProductDetailsResponse(BillingResult billingResult, QueryProductDetailsResult result) {
                    removeAdsProductDetailsQueryRunning = false;
                    if (billingResult == null || billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK || result == null) {
                        if (pendingRemoveAdsPurchaseLaunch) {
                            pendingRemoveAdsPurchaseLaunch = false;
                            showBillingFailure("queryRemoveAdsProductDetails", billingResult);
                        } else {
                            logBillingResult("queryRemoveAdsProductDetails", billingResult);
                        }
                        return;
                    }
                    List<ProductDetails> list = result.getProductDetailsList();
                    ProductDetails matchingProduct = null;
                    if (list != null) {
                        for (ProductDetails details : list) {
                            if (details != null && REMOVE_ADS_PRODUCT_ID.equals(details.getProductId())) {
                                matchingProduct = details;
                            }
                        }
                    }
                    removeAdsProductDetails = matchingProduct;
                    List<UnfetchedProduct> unfetchedProducts = result.getUnfetchedProductList();
                    if (unfetchedProducts != null) {
                        for (UnfetchedProduct product : unfetchedProducts) {
                            if (product != null && REMOVE_ADS_PRODUCT_ID.equals(product.getProductId())) {
                                android.util.Log.w(
                                        "ToxicBilling",
                                        product.getProductId() + " unfetched: status=" + product.getStatusCode()
                                                + ", type=" + product.getProductType()
                                );
                            }
                        }
                    }
                    if (pendingRemoveAdsPurchaseLaunch && removeAdsProductDetails == null) {
                        pendingRemoveAdsPurchaseLaunch = false;
                        runOnUiThread(() -> toast(t(R.string.purchase_unavailable)));
                    } else if (pendingRemoveAdsPurchaseLaunch) {
                        runOnUiThread(() -> launchRemoveAdsPurchase());
                    }
                }
            });
        } catch(Exception e) {
            removeAdsProductDetailsQueryRunning = false;
            android.util.Log.w("ToxicBilling", "queryRemoveAdsProductDetails exception", e);
            if (pendingRemoveAdsPurchaseLaunch) {
                pendingRemoveAdsPurchaseLaunch = false;
                showBillingFailure("queryRemoveAdsProductDetails", null);
            }
        }
    }

    private void querySupporterProductDetails() {
        try {
            if (billingClient == null || !billingClient.isReady() || supporterProductDetailsQueryRunning) return;
            supporterProductDetailsQueryRunning = true;
            ArrayList<QueryProductDetailsParams.Product> products = new ArrayList<>();
            products.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(SUPPORTER_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build());
            QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                    .setProductList(products)
                    .build();
            billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
                @Override public void onProductDetailsResponse(BillingResult billingResult, QueryProductDetailsResult result) {
                    supporterProductDetailsQueryRunning = false;
                    if (billingResult == null || billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK || result == null) {
                        if (pendingSupporterPurchaseLaunch) {
                            pendingSupporterPurchaseLaunch = false;
                            if (billingResult != null
                                    && billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                                runOnUiThread(() -> toast(t(R.string.supporter_unavailable)));
                            } else {
                                showBillingFailure("querySupporterProductDetails", billingResult);
                            }
                        } else {
                            logBillingResult("querySupporterProductDetails", billingResult);
                        }
                        return;
                    }
                    ProductDetails matchingSupporter = null;
                    List<ProductDetails> list = result.getProductDetailsList();
                    if (list != null) {
                        for (ProductDetails details : list) {
                            if (details != null && SUPPORTER_PRODUCT_ID.equals(details.getProductId())) {
                                matchingSupporter = details;
                                break;
                            }
                        }
                    }
                    supporterProductDetails = matchingSupporter;
                    List<UnfetchedProduct> unfetchedProducts = result.getUnfetchedProductList();
                    if (unfetchedProducts != null) {
                        for (UnfetchedProduct product : unfetchedProducts) {
                            if (product != null && SUPPORTER_PRODUCT_ID.equals(product.getProductId())) {
                                android.util.Log.w(
                                        "ToxicBilling",
                                        product.getProductId() + " unfetched: status=" + product.getStatusCode()
                                                + ", type=" + product.getProductType()
                                );
                            }
                        }
                    }
                    if (pendingSupporterPurchaseLaunch && supporterProductDetails == null) {
                        pendingSupporterPurchaseLaunch = false;
                        runOnUiThread(() -> toast(t(R.string.supporter_unavailable)));
                    } else if (pendingSupporterPurchaseLaunch) {
                        runOnUiThread(() -> launchSupporterPurchase());
                    }
                }
            });
        } catch(Exception e) {
            supporterProductDetailsQueryRunning = false;
            android.util.Log.w("ToxicBilling", "querySupporterProductDetails exception", e);
            if (pendingSupporterPurchaseLaunch) {
                pendingSupporterPurchaseLaunch = false;
                showBillingFailure("querySupporterProductDetails", null);
            }
        }
    }

    private void queryRemoveAdsPurchases() {
        try {
            if (billingClient == null || !billingClient.isReady()) return;
            QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build();
            billingClient.queryPurchasesAsync(params, (billingResult, purchases) -> {
                if (billingResult == null || billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
                boolean owned = false;
                if (purchases != null) {
                    for (Purchase purchase : purchases) {
                        if (isRemoveAdsPurchase(purchase) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            owned = true;
                        }
                    }
                    handleRemoveAdsPurchases(purchases, false);
                }
                setRemoveAdsPurchased(owned);
            });
        } catch(Exception ignored) {}
    }

    private void querySupporterPurchases() {
        try {
            if (billingClient == null || !billingClient.isReady() || supporterPurchaseQueryRunning) return;
            supporterPurchaseQueryRunning = true;
            QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build();
            billingClient.queryPurchasesAsync(params, (billingResult, purchases) -> {
                supporterPurchaseQueryRunning = false;
                if (billingResult == null || billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
                boolean owned = false;
                if (purchases != null) {
                    for (Purchase purchase : purchases) {
                        if (isSupporterPurchase(purchase)
                                && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            owned = true;
                            break;
                        }
                    }
                    handleSupporterPurchases(purchases, false);
                }
                if (!owned) {
                    supporterPurchaseToken = "";
                    supporterExpiresAtMs = 0L;
                    supporterNextVerificationAtMs = 0L;
                    finishBillingEntitlementCheck(false);
                }
            });
        } catch(Exception ignored) {
            supporterPurchaseQueryRunning = false;
        }
    }

    private boolean isSupporterPurchase(Purchase purchase) {
        if (purchase == null) return false;
        try {
            return purchase.getProducts() != null
                    && purchase.getProducts().contains(SUPPORTER_PRODUCT_ID);
        } catch(Exception ignored) {
            return false;
        }
    }

    private void handleSupporterPurchases(List<Purchase> purchases, boolean showToast) {
        if (purchases == null) return;
        for (Purchase purchase : purchases) {
            if (!isSupporterPurchase(purchase)) continue;
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                supporterPurchaseToken = purchase.getPurchaseToken() == null
                        ? ""
                        : purchase.getPurchaseToken().trim();
                if (showToast) {
                    getSharedPreferences(PREFS, MODE_PRIVATE)
                            .edit()
                            .putBoolean(PREF_SUPPORTER_TUTORIAL_PENDING, true)
                            .apply();
                }
                if (!purchase.isAcknowledged() && billingClient != null) {
                    try {
                        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                        billingClient.acknowledgePurchase(params, billingResult -> {});
                    } catch(Exception ignored) {}
                }
                syncSupporterStatusWithBackend(supporterPurchaseToken, showToast);
            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING && showToast) {
                runOnUiThread(() -> toast(t(R.string.supporter_pending)));
            }
        }
    }

    private void finishBillingEntitlementCheck(boolean supporterOwned) {
        billingEntitlementCheckPending = false;
        if (billingEntitlementTimeoutRunnable != null) {
            uiHandler.removeCallbacks(billingEntitlementTimeoutRunnable);
            billingEntitlementTimeoutRunnable = null;
        }
        setSupporterActive(supporterOwned);
        if (!hasAdFreeAccess()) {
            preloadBannerAds();
            resumeBannerAds();
            // Profile slots may have been attached while billingEntitlementCheckPending was true.
            // Force a fresh load after entitlement is known instead of relying on the initial request.
            uiHandler.postDelayed(this::refreshAttachedProfileBannerAds, 120L);
            uiHandler.postDelayed(this::refreshAttachedProfileBannerAds, 900L);
            loadInterstitialAd();
            loadRewardedAd();
            loadStartNativeAdIfNeeded();
            uiHandler.postDelayed(this::maybeShowPendingProfileInterstitial, 120L);
        } else {
            pendingProfileInterstitialAction = false;
            pendingProfileInterstitialRequestedAt = 0L;
        }
    }

    private void setSupporterActive(boolean active) {
        boolean changed = supporterActive != active;
        supporterActive = active;
        if (active) {
            pendingProfileInterstitialAction = false;
            pendingProfileInterstitialRequestedAt = 0L;
            cancelInterstitialAdRetry();
            cancelRewardedAdRetry();
            destroyAllBannerAds();
            interstitialAd = null;
            rewardedAd = null;
            interstitialLoading = false;
            interstitialShowing = false;
            rewardedLoading = false;
        } else if (changed && !billingEntitlementCheckPending && !hasAdFreeAccess()) {
            preloadBannerAds();
            loadInterstitialAd();
            loadRewardedAd();
            loadStartNativeAdIfNeeded();
        }
        runOnUiThread(() -> {
            updateRewardButtonText();
            updateSponsorsSubscribeButton();
        });
    }

    private void refreshSupporterEntitlementIfNeeded() {
        if (!supporterActive || !appInForeground) return;
        long now = System.currentTimeMillis();
        if (supporterExpiresAtMs > 0L && now >= supporterExpiresAtMs) {
            supporterExpiresAtMs = 0L;
            supporterNextVerificationAtMs = now + 60_000L;
            setSupporterActive(false);
            querySupporterPurchases();
            return;
        }
        if (now >= supporterNextVerificationAtMs) {
            supporterNextVerificationAtMs = now + SUPPORTER_REVERIFY_INTERVAL_MS;
            querySupporterPurchases();
        }
    }

    private ProductDetails.SubscriptionOfferDetails basicSupporterOffer() {
        if (supporterProductDetails == null) return null;
        try {
            List<ProductDetails.SubscriptionOfferDetails> offers = supporterProductDetails.getSubscriptionOfferDetails();
            if (offers == null) return null;
            for (ProductDetails.SubscriptionOfferDetails offer : offers) {
                if (offer != null && SUPPORTER_BASE_PLAN_ID.equals(offer.getBasePlanId())) return offer;
            }
        } catch(Exception ignored) {}
        return null;
    }

    private String supporterPriceText() {
        ProductDetails.SubscriptionOfferDetails offer = basicSupporterOffer();
        if (offer == null) return "";
        try {
            List<ProductDetails.PricingPhase> phases = offer.getPricingPhases().getPricingPhaseList();
            if (phases == null || phases.isEmpty()) return "";
            ProductDetails.PricingPhase phase = phases.get(phases.size() - 1);
            return phase == null ? "" : phase.getFormattedPrice();
        } catch(Exception ignored) {
            return "";
        }
    }

    private void launchSupporterPurchase() {
        try {
            if (supporterActive) {
                showSupporterManageDialog();
                return;
            }
            if (billingClient == null || !billingClient.isReady()) {
                pendingSupporterPurchaseLaunch = true;
                ensureBillingReady();
                uiHandler.postDelayed(() -> {
                    if (pendingSupporterPurchaseLaunch && (billingClient == null || !billingClient.isReady())) {
                        toast(t(R.string.purchase_loading));
                    }
                }, 1800L);
                return;
            }
            if (supporterProductDetails == null) {
                pendingSupporterPurchaseLaunch = true;
                querySupporterProductDetails();
                return;
            }
            ProductDetails.SubscriptionOfferDetails offer = basicSupporterOffer();
            if (offer == null || offer.getOfferToken() == null || offer.getOfferToken().trim().isEmpty()) {
                pendingSupporterPurchaseLaunch = false;
                toast(t(R.string.supporter_unavailable));
                return;
            }
            pendingSupporterPurchaseLaunch = false;
            BillingFlowParams.ProductDetailsParams productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(supporterProductDetails)
                    .setOfferToken(offer.getOfferToken())
                    .build();
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(Collections.singletonList(productParams))
                    .build();
            BillingResult result = billingClient.launchBillingFlow(this, flowParams);
            if (result == null || result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                if (result != null && result.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                    querySupporterPurchases();
                } else {
                    showBillingFailure("launchSupporterBillingFlow", result);
                }
            }
        } catch(Exception e) {
            android.util.Log.w("ToxicBilling", "launchSupporterPurchase exception", e);
            toast(t(R.string.purchase_error));
        }
    }

    private boolean isRemoveAdsPurchase(Purchase purchase) {
        if (purchase == null) return false;
        try { return purchase.getProducts() != null && purchase.getProducts().contains(REMOVE_ADS_PRODUCT_ID); } catch(Exception ignored) { return false; }
    }

    private void handleRemoveAdsPurchases(List<Purchase> purchases, boolean showToast) {
        if (purchases == null) return;
        for (Purchase purchase : purchases) {
            if (!isRemoveAdsPurchase(purchase)) continue;
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                setRemoveAdsPurchased(true);
                if (!purchase.isAcknowledged()) {
                    try {
                        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                        billingClient.acknowledgePurchase(params, billingResult -> {});
                    } catch(Exception ignored) {}
                }
                if (showToast) runOnUiThread(() -> toast(t(R.string.remove_ads_purchased)));
            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING && showToast) {
                runOnUiThread(() -> toast(t(R.string.purchase_pending)));
            }
        }
    }

    private void setRemoveAdsPurchased(boolean purchased) {
        if (removeAdsPurchased == purchased) {
            runOnUiThread(this::updateRewardButtonText);
            return;
        }
        removeAdsPurchased = purchased;
        if (purchased) {
            cancelInterstitialAdRetry();
            cancelRewardedAdRetry();
            destroyAllBannerAds();
            interstitialAd = null;
            rewardedAd = null;
            interstitialLoading = false;
            interstitialShowing = false;
            rewardedLoading = false;
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(PREF_REMOVE_ADS_PURCHASED, purchased).apply();
        runOnUiThread(this::updateRewardButtonText);
    }

    private void launchRemoveAdsPurchase() {
        try {
            if (removeAdsPurchased) {
                toast(t(R.string.remove_ads_purchased));
                updateRewardButtonText();
                return;
            }
            if (billingClient == null || !billingClient.isReady()) {
                pendingRemoveAdsPurchaseLaunch = true;
                ensureBillingReady();
                uiHandler.postDelayed(() -> { if (pendingRemoveAdsPurchaseLaunch && (billingClient == null || !billingClient.isReady())) toast(t(R.string.purchase_loading)); }, 1800L);
                return;
            }
            if (removeAdsProductDetails == null) {
                pendingRemoveAdsPurchaseLaunch = true;
                queryRemoveAdsProductDetails();
                return;
            }
            pendingRemoveAdsPurchaseLaunch = false;
            BillingFlowParams.ProductDetailsParams.Builder productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(removeAdsProductDetails);
            try {
                List<ProductDetails.OneTimePurchaseOfferDetails> offers = removeAdsProductDetails.getOneTimePurchaseOfferDetailsList();
                if (offers != null && !offers.isEmpty()) {
                    String offerToken = offers.get(0).getOfferToken();
                    if (offerToken != null && !offerToken.trim().isEmpty()) productParamsBuilder.setOfferToken(offerToken);
                }
            } catch(Exception ignored) {}
            ArrayList<BillingFlowParams.ProductDetailsParams> productParams = new ArrayList<>();
            productParams.add(productParamsBuilder.build());
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productParams)
                    .build();
            BillingResult result = billingClient.launchBillingFlow(this, flowParams);
            if (result == null || result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                if (result != null && result.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                    queryRemoveAdsPurchases();
                } else {
                    pendingRemoveAdsPurchaseLaunch = false;
                    showBillingFailure("launchBillingFlow", result);
                }
            }
        } catch(Exception e) {
            android.util.Log.w("ToxicBilling", "launchBillingFlow exception", e);
            toast(t(R.string.purchase_error));
        }
    }

    private void showRewardedAdDialog() {
        if (accessGateReason != AccessGateReason.NONE) return;
        loadRewardedAd();
        consumeAdFreeElapsed();
        String remaining = formatAdFreeRemaining();
        String message = hasAdFreeAccess()
                ? tr(R.string.adfree_msg_add, remaining, rewardedAdsWatched, REWARDED_ADS_REQUIRED)
                : tr(R.string.adfree_msg_new, rewardedAdsWatched, REWARDED_ADS_REQUIRED);

        final Dialog dialog = new Dialog(this);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(18), dp(18), dp(18), dp(18));
        wrap.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        dialog.setContentView(wrap);
        applySafeAreaInsets(dialog.getWindow(), wrap);

        LinearLayout iconLine = new LinearLayout(this);
        iconLine.setGravity(Gravity.CENTER);
        ImageView icon = new ImageView(this);
        icon.setImageDrawable(new RewardVideoDrawable());
        iconLine.addView(icon, new LinearLayout.LayoutParams(dp(54), dp(54)));
        wrap.addView(iconLine, lp(-1, dp(58), 0, 0, 0, 10));

        TextView title = toxicLogoText(t(R.string.adfree_title), 21);
        title.setGravity(Gravity.CENTER);
        wrap.addView(title, lp(-1, -2, 0, 0, 0, 10));

        TextView msg = text(message, 14, lightTheme ? Color.rgb(55,55,55) : Color.argb(226,255,255,255), false);
        msg.setGravity(Gravity.CENTER);
        msg.setLineSpacing(dp(3), 1f);
        msg.setPadding(dp(8), dp(8), dp(8), dp(8));
        msg.setBackground(round(lightTheme ? Color.rgb(246,246,248) : Color.argb(18,255,255,255), dp(16), lightTheme ? Color.rgb(222,222,226) : Color.argb(28,255,255,255), 1));
        wrap.addView(msg, lp(-1, -2, 0, 0, 0, 14));

        if (hasAdFreeAccess()) {
            TextView timer = text(t(R.string.time_left) + ": " + formatAdFreeRemainingShort(), 13, lightTheme ? Color.rgb(50,50,50) : Color.WHITE, true);
            timer.setGravity(Gravity.CENTER);
            timer.setPadding(dp(10), dp(8), dp(10), dp(8));
            timer.setBackground(round(lightTheme ? Color.rgb(238,238,242) : Color.argb(24,255,255,255), dp(999), lightTheme ? Color.rgb(216,216,222) : Color.argb(30,255,255,255), 1));
            wrap.addView(timer, lp(-1, -2, 0, 0, 0, 14));
        }

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);
        wrap.addView(buttons, lp(-1, dp(48), 0, 0, 0, 0));

        TextView cancel = dialogButton(t(R.string.cancel));
        cancel.setTextColor(lightTheme ? Color.rgb(45,45,45) : Color.WHITE);
        cancel.setBackground(round(lightTheme ? Color.rgb(242,242,244) : Color.argb(18,255,255,255), dp(14), lightTheme ? Color.rgb(216,216,220) : Color.argb(30,255,255,255), 1));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, dp(48), 1);
        cp.rightMargin = dp(6);
        buttons.addView(cancel, cp);
        cancel.setOnClickListener(v -> dialog.dismiss());

        TextView watch = dialogButton(tr(
                R.string.watch_video,
                Math.min(REWARDED_ADS_REQUIRED, rewardedAdsWatched + 1),
                REWARDED_ADS_REQUIRED
        ));
        watch.setTextColor(Color.WHITE);
        watch.setSingleLine(true);
        if (Build.VERSION.SDK_INT >= 26) {
            watch.setAutoSizeTextTypeUniformWithConfiguration(
                    9,
                    15,
                    1,
                    android.util.TypedValue.COMPLEX_UNIT_SP
            );
        } else {
            watch.setTextSize(12);
        }
        watch.setBackground(grad(dp(14), purple2, purple));
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(0, dp(48), 1);
        wp.leftMargin = dp(6);
        buttons.addView(watch, wp);
        watch.setOnClickListener(v -> {
            dialog.dismiss();
            showRewardedAdForAdFreeTime();
        });

        View buyNoAds = buildNoAdsPurchaseBanner();
        LinearLayout.LayoutParams buyLp = new LinearLayout.LayoutParams(-1, dp(94));
        buyLp.topMargin = dp(12);
        wrap.addView(buyNoAds, buyLp);
        buyNoAds.setOnClickListener(v -> {
            dialog.dismiss();
            launchRemoveAdsPurchase();
        });

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(28), dp(430));
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(params);
        }
    }

    private View buildNoAdsPurchaseBanner() {
        FrameLayout banner = new FrameLayout(this);
        banner.setBackground(new NoAdsBannerDrawable());
        banner.setClickable(true);
        banner.setFocusable(true);
        banner.setPadding(dp(10), dp(8), dp(10), dp(8));

        ImageView crown = new ImageView(this);
        crown.setImageDrawable(new PremiumCrownDrawable());
        FrameLayout.LayoutParams crownLp = new FrameLayout.LayoutParams(dp(40), dp(40), Gravity.LEFT | Gravity.CENTER_VERTICAL);
        crownLp.leftMargin = dp(11);
        banner.addView(crown, crownLp);

        ImageView arrow = new ImageView(this);
        arrow.setImageDrawable(new PremiumArrowDrawable());
        FrameLayout.LayoutParams arrowLp = new FrameLayout.LayoutParams(dp(40), dp(40), Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        arrowLp.rightMargin = dp(11);
        banner.addView(arrow, arrowLp);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams textLp = new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER_VERTICAL);
        textLp.leftMargin = dp(60);
        textLp.rightMargin = dp(54);
        banner.addView(texts, textLp);

        TextView title = text(t(R.string.premium_title), 16, Color.WHITE, true);
        title.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        title.setSingleLine(false);
        title.setMaxLines(2);
        title.setIncludeFontPadding(false);
        if (Build.VERSION.SDK_INT >= 26) title.setAutoSizeTextTypeUniformWithConfiguration(9, 16, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
        texts.addView(title, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout subtitle = new LinearLayout(this);
        subtitle.setGravity(Gravity.CENTER_VERTICAL);
        subtitle.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.topMargin = dp(4);
        texts.addView(subtitle, subLp);

        ImageView adIcon = new ImageView(this);
        adIcon.setImageDrawable(new TinyNoAdDrawable());
        LinearLayout.LayoutParams adLp = new LinearLayout.LayoutParams(dp(17), dp(17));
        adLp.rightMargin = dp(6);
        subtitle.addView(adIcon, adLp);

        TextView sub = text(t(R.string.premium_remove_ads), 12, Color.argb(232,255,255,255), false);
        sub.setSingleLine(false);
        sub.setMaxLines(2);
        sub.setIncludeFontPadding(false);
        if (Build.VERSION.SDK_INT >= 26) sub.setAutoSizeTextTypeUniformWithConfiguration(7, 12, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
        subtitle.addView(sub, new LinearLayout.LayoutParams(0, -2, 1));

        TextView chip = text("✦  " + t(R.string.premium_pay_once), 8, Color.rgb(50, 38, 8), true);
        chip.setGravity(Gravity.CENTER);
        chip.setSingleLine(true);
        chip.setMaxLines(1);
        chip.setIncludeFontPadding(false);
        chip.setPadding(dp(8), 0, dp(8), 0);
        chip.setBackground(round(Color.rgb(255, 193, 24), dp(999), Color.argb(80,255,255,255), 1));
        if (Build.VERSION.SDK_INT >= 26) chip.setAutoSizeTextTypeUniformWithConfiguration(6, 8, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
        LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(-2, dp(18));
        chipLp.topMargin = dp(5);
        texts.addView(chip, chipLp);
        return banner;
    }

    private void showRewardedAdForAdFreeTime() {
        if (accessGateReason != AccessGateReason.NONE) return;
        if (getAdFreeRemainingMs() >= MAX_AD_FREE_MS) {
            toast(t(R.string.limit_24h));
            updateRewardButtonText();
            return;
        }

        if (rewardedAd == null) {
            toast(t(R.string.video_loading));
            loadRewardedAd();
            return;
        }

        rewardedAd.show(this, (RewardItem rewardItem) -> handleRewardedAdEarned());
    }

    private void handleRewardedAdEarned() {
        rewardedAdsWatched = Math.min(REWARDED_ADS_REQUIRED, rewardedAdsWatched + 1);
        if (rewardedAdsWatched >= REWARDED_ADS_REQUIRED) {
            rewardedAdsWatched = 0;
            saveRewardedAdsWatched();
            grantAdFreeTime(REWARDED_AD_FREE_MS);
            return;
        }
        saveRewardedAdsWatched();
        updateRewardButtonText();
        toast(tr(
                R.string.reward_progress,
                rewardedAdsWatched,
                REWARDED_ADS_REQUIRED
        ));
    }

    private void grantAdFreeTime(long millis) {
        long now = System.currentTimeMillis();
        long remaining = getAdFreeRemainingMs();
        long updatedRemaining = Math.min(MAX_AD_FREE_MS, Math.max(0L, remaining) + millis);
        adFreeUntilMs = now + updatedRemaining;
        saveAdFreeUntil();
        pendingProfileInterstitialAction = false;
        pendingProfileInterstitialRequestedAt = 0L;
        cancelInterstitialAdRetry();
        destroyAllBannerAds();
        updateRewardButtonText();
        toast(t(R.string.adfree_granted));
    }

    private boolean hasConfirmedAdFreeAccess() {
        return removeAdsPurchased
                || supporterActive
                || getAdFreeRemainingMs() > 0L;
    }

    private boolean hasAdFreeAccess() {
        return hasConfirmedAdFreeAccess()
                || billingEntitlementCheckPending;
    }

    private long getAdFreeRemainingMs() {
        long now = System.currentTimeMillis();
        long remaining = Math.max(0L, adFreeUntilMs - now);
        if (remaining <= 0L && adFreeUntilMs != 0L) {
            adFreeUntilMs = 0L;
            saveAdFreeUntil();
        }
        return remaining;
    }

    private void consumeAdFreeElapsed() {
        getAdFreeRemainingMs();
    }

    private void saveAdFreeUntil() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putLong(PREF_AD_FREE_UNTIL_MS, Math.max(0L, adFreeUntilMs)).apply();
    }

    private void saveRewardedAdsWatched() {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putInt(PREF_REWARDED_ADS_WATCHED, Math.max(0, Math.min(REWARDED_ADS_REQUIRED - 1, rewardedAdsWatched)))
                .apply();
    }

    private void saveAdFreeRemaining() {
        saveAdFreeUntil();
    }

    private void updateRewardButtonText() {
        if (rewardAdBtn == null) return;
        if (supporterActive) {
            rewardAdBtn.setVisibility(View.VISIBLE);
            rewardAdBtn.setText("");
            rewardAdBtn.setBackground(new SupporterProfileButtonDrawable());
            rewardAdBtn.setContentDescription(t(R.string.supporter_choose_profile));
            if (rewardAdTimeLabel != null) {
                rewardAdTimeLabel.setText("");
                rewardAdTimeLabel.setVisibility(View.GONE);
            }
            return;
        }
        if (removeAdsPurchased) {
            rewardAdBtn.setVisibility(View.GONE);
            if (rewardAdTimeLabel != null) rewardAdTimeLabel.setVisibility(View.GONE);
            return;
        }
        rewardAdBtn.setVisibility(View.VISIBLE);
        rewardAdBtn.setBackground(new RewardVideoDrawable());
        rewardAdBtn.setContentDescription(t(R.string.adfree_title));
        long remainingMs = getAdFreeRemainingMs();

        rewardAdBtn.setText("");
        rewardAdBtn.setTextColor(Color.WHITE);

        if (rewardAdTimeLabel != null) {
            if (remainingMs > 0L) {
                rewardAdTimeLabel.setText(formatAdFreeRemainingShort());
                rewardAdTimeLabel.setTextColor(lightTheme ? Color.rgb(45,45,45) : Color.WHITE);
                rewardAdTimeLabel.setVisibility(View.VISIBLE);
            } else {
                rewardAdTimeLabel.setText(rewardedAdsWatched + "/" + REWARDED_ADS_REQUIRED);
                rewardAdTimeLabel.setTextColor(lightTheme ? Color.rgb(45,45,45) : Color.WHITE);
                rewardAdTimeLabel.setVisibility(View.VISIBLE);
            }
        }
    }

    private String formatAdFreeRemainingShort() {
        long totalSeconds = Math.max(0L, getAdFreeRemainingMs()) / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0L) return tr(R.string.duration_short_hours, hours, minutes);
        return tr(R.string.duration_short_minutes, minutes, seconds);
    }

    private String formatAdFreeRemaining() {
        long totalSeconds = Math.max(0L, getAdFreeRemainingMs()) / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0L) return tr(R.string.duration_hours_minutes, hours, minutes);
        if (minutes > 0L) return tr(R.string.duration_minutes_seconds, minutes, seconds);
        return tr(R.string.duration_seconds, seconds);
    }

    @Override protected void onResume() {
        super.onResume();
        appInForeground = true;
        if (!accessProbeRunning) requestAccessGateCheck();
        resumeBannerAds();
        if (removeAdsPurchased || hasAdFreeAccess()) destroyAllBannerAds();
        else {
            preloadBannerAds();
            loadStartNativeAdIfNeeded();
        }
        loadFavoriteOnlineStatesFromPrefs();
        updateFavoriteOnlineBadgeText();
        uiHandler.removeCallbacks(adFreeTicker);
        uiHandler.post(adFreeTicker);
        startFavoriteOnlineWatcher();
        ensureBillingReady();
        queryRemoveAdsPurchases();
        querySupporterPurchases();
        refreshSponsors();
        if (!removeAdsPurchased && !supporterActive) {
            if (!hasAdFreeAccess()) loadInterstitialAd();
            loadRewardedAd();
        }
        checkFavoriteOnlineNotifications();
    }

    @Override protected void onPause() {
        appInForeground = false;
        uiHandler.removeCallbacks(accessGateRecheckRunnable);
        pauseBannerAds();
        cancelAllBannerAdRetries();
        saveAdFreeUntil();
        uiHandler.removeCallbacks(adFreeTicker);
        cancelInterstitialAdRetry();
        cancelRewardedAdRetry();
        startFavoriteOnlineWatcher();
        super.onPause();
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        translationContext = null;
        translationContextHotel = "";
        applySystemBarsForTheme();
        if (screen != null) {
            screen.requestLayout();
            screen.post(() -> {
                screen.requestLayout();
                screen.invalidate();
                if (tutorialOverlayView != null) tutorialOverlayView.invalidate();
                if (visualTutorialOverlayView != null) visualTutorialOverlayView.invalidate();
                if (mainScroll != null) mainScroll.requestLayout();
            });
        }
    }

    @Override protected void onDestroy() {
        saveAdFreeUntil();
        cancelTutorialPulseAnimation();
        stopAccessGateMonitoring();
        if (suggestionDebounceTask != null) uiHandler.removeCallbacks(suggestionDebounceTask);
        if (billingEntitlementTimeoutRunnable != null) uiHandler.removeCallbacks(billingEntitlementTimeoutRunnable);
        uiHandler.removeCallbacks(adFreeTicker);
        cancelInterstitialAdRetry();
        cancelRewardedAdRetry();
        destroyAllBannerAds();
        if (favoriteOnlineWatcher != null) uiHandler.removeCallbacks(favoriteOnlineWatcher);
        try { if (billingClient != null && billingClient.isReady()) billingClient.endConnection(); } catch(Exception ignored) {}
        profileSectionsExecutor.shutdownNow();
        executor.shutdownNow();
        destroyHabbodexWebTransport();
        super.onDestroy();
    }

    private void startAccessGateMonitoring() {
        accessConnectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        if (accessConnectivityManager == null || accessNetworkCallbackRegistered) {
            requestAccessGateCheck();
            return;
        }
        accessNetworkCallback = new ConnectivityManager.NetworkCallback() {
            private void changed() {
                uiHandler.post(MainActivity.this::requestAccessGateCheck);
            }

            @Override public void onAvailable(Network network) { changed(); }
            @Override public void onLost(Network network) { changed(); }
            @Override public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) { changed(); }
            @Override public void onLinkPropertiesChanged(Network network, LinkProperties properties) { changed(); }
        };
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                accessConnectivityManager.registerDefaultNetworkCallback(accessNetworkCallback);
            } else {
                NetworkRequest request = new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build();
                accessConnectivityManager.registerNetworkCallback(request, accessNetworkCallback);
            }
            accessNetworkCallbackRegistered = true;
        } catch(Exception ignored) {
            accessNetworkCallbackRegistered = false;
        }
        requestAccessGateCheck();
    }

    private void stopAccessGateMonitoring() {
        accessProbeGeneration++;
        accessProbeRerunRequested = false;
        uiHandler.removeCallbacks(accessGateRecheckRunnable);
        if (accessNetworkCallbackRegistered && accessConnectivityManager != null && accessNetworkCallback != null) {
            try { accessConnectivityManager.unregisterNetworkCallback(accessNetworkCallback); } catch(Exception ignored) {}
        }
        accessNetworkCallbackRegistered = false;
        accessNetworkCallback = null;
        dismissAccessGate();
    }

    private void requestAccessGateCheck() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            uiHandler.post(this::requestAccessGateCheck);
            return;
        }
        uiHandler.removeCallbacks(accessGateRecheckRunnable);
        if (!appInForeground || isFinishing() || (Build.VERSION.SDK_INT >= 17 && isDestroyed())) return;

        NetworkCapabilities capabilities = currentAccessNetworkCapabilities();
        boolean configuredForInternet = capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        boolean captivePortal = capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL);
        boolean validated = capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

        if (!configuredForInternet || captivePortal) {
            accessProbeGeneration++;
            if (accessProbeRunning) accessProbeRerunRequested = true;
            showAccessGate(AccessGateReason.OFFLINE);
            scheduleNextAccessGateCheck();
            return;
        }

        if (isKnownAdBlockingDnsActive()) {
            accessProbeGeneration++;
            if (accessProbeRunning) accessProbeRerunRequested = true;
            showAccessGate(AccessGateReason.AD_BLOCKER);
            scheduleNextAccessGateCheck();
            return;
        }

        // Enquanto o Android ainda não validou a rede, bloqueia a interface.
        // A sondagem abaixo distingue falta de acesso do bloqueio aos anúncios.
        if (!validated) showAccessGate(AccessGateReason.OFFLINE);

        if (accessProbeRunning) {
            accessProbeGeneration++;
            accessProbeRerunRequested = true;
            return;
        }

        accessProbeRunning = true;
        accessProbeRerunRequested = false;
        final int generation = ++accessProbeGeneration;
        executor.execute(() -> {
            AccessProbeResult result = performAccessProbe();
            uiHandler.post(() -> {
                accessProbeRunning = false;
                if (!appInForeground || isFinishing() || (Build.VERSION.SDK_INT >= 17 && isDestroyed())) return;
                if (generation != accessProbeGeneration) {
                    if (accessProbeRerunRequested) {
                        accessProbeRerunRequested = false;
                        requestAccessGateCheck();
                    }
                    return;
                }

                NetworkCapabilities latest = currentAccessNetworkCapabilities();
                boolean stillConfigured = latest != null
                        && latest.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && !latest.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL);
                if (!stillConfigured || !result.appInternetReachable) {
                    showAccessGate(AccessGateReason.OFFLINE);
                } else if (isKnownAdBlockingDnsActive() || !result.adServicesReachable) {
                    showAccessGate(AccessGateReason.AD_BLOCKER);
                } else {
                    dismissAccessGate();
                }

                if (accessProbeRerunRequested) {
                    accessProbeRerunRequested = false;
                    requestAccessGateCheck();
                } else {
                    scheduleNextAccessGateCheck();
                }
            });
        });
    }

    private NetworkCapabilities currentAccessNetworkCapabilities() {
        try {
            if (accessConnectivityManager == null) {
                accessConnectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            }
            if (accessConnectivityManager == null) return null;
            Network active = accessConnectivityManager.getActiveNetwork();
            return active == null ? null : accessConnectivityManager.getNetworkCapabilities(active);
        } catch(Exception ignored) {
            return null;
        }
    }

    private LinkProperties currentAccessLinkProperties() {
        try {
            if (accessConnectivityManager == null) {
                accessConnectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            }
            if (accessConnectivityManager == null) return null;
            Network active = accessConnectivityManager.getActiveNetwork();
            return active == null ? null : accessConnectivityManager.getLinkProperties(active);
        } catch(Exception ignored) {
            return null;
        }
    }

    private boolean isKnownAdBlockingDnsActive() {
        LinkProperties properties = currentAccessLinkProperties();
        if (properties == null) return false;
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                String privateDns = properties.getPrivateDnsServerName();
                String normalized = privateDns == null ? "" : privateDns.trim().toLowerCase(Locale.ROOT);
                if (normalized.contains("adguard") && !normalized.contains("unfiltered")) return true;
            }
            for (InetAddress dns : properties.getDnsServers()) {
                if (dns == null) continue;
                String address = dns.getHostAddress();
                if (address == null) continue;
                address = address.toLowerCase(Locale.ROOT);
                int zone = address.indexOf('%');
                if (zone >= 0) address = address.substring(0, zone);
                if ("94.140.14.14".equals(address)
                        || "94.140.15.15".equals(address)
                        || "94.140.14.15".equals(address)
                        || "94.140.15.16".equals(address)) return true;
                if (address.startsWith("2a10:50c0:")
                        && (address.endsWith(":ad1:ff")
                        || address.endsWith(":ad2:ff")
                        || address.endsWith(":bad1:ff")
                        || address.endsWith(":bad2:ff"))) return true;
            }
        } catch(Exception ignored) {}
        return false;
    }

    private AccessProbeResult performAccessProbe() {
        boolean appInternetReachable = false;
        for (String url : ACCESS_GATE_CONTROL_URLS) {
            int code = probeHttpResponseCode(url);
            if (code > 0) {
                appInternetReachable = true;
                break;
            }
        }
        if (!appInternetReachable) return new AccessProbeResult(false, false);

        boolean adServicesReachable = false;
        for (String[] probe : ACCESS_GATE_AD_PROBES) {
            if (probe == null || probe.length < 2 || !hostResolvesPublicly(probe[0])) continue;
            if (probeHttpResponseCode(probe[1]) == HttpURLConnection.HTTP_NO_CONTENT) {
                adServicesReachable = true;
                break;
            }
        }
        return new AccessProbeResult(true, adServicesReachable);
    }

    private boolean hostResolvesPublicly(String host) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            if (addresses == null || addresses.length == 0) return false;
            for (InetAddress address : addresses) {
                if (address == null) continue;
                if (!address.isAnyLocalAddress()
                        && !address.isLoopbackAddress()
                        && !address.isLinkLocalAddress()
                        && !address.isSiteLocalAddress()) return true;
            }
        } catch(Exception ignored) {}
        return false;
    }

    private int probeHttpResponseCode(String rawUrl) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)new URL(rawUrl).openConnection();
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.setConnectTimeout(ACCESS_GATE_CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(ACCESS_GATE_READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Cache-Control", "no-cache, no-store");
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("User-Agent", "ToxicSearchTool/" + APP_VERSION + " Android");
            return connection.getResponseCode();
        } catch(Exception ignored) {
            return -1;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void scheduleNextAccessGateCheck() {
        uiHandler.removeCallbacks(accessGateRecheckRunnable);
        if (!appInForeground) return;
        long delay = accessGateReason == AccessGateReason.NONE
                ? ACCESS_GATE_CLEAR_RECHECK_MS
                : ACCESS_GATE_BLOCKED_RECHECK_MS;
        uiHandler.postDelayed(accessGateRecheckRunnable, delay);
    }

    private void showAccessGate(AccessGateReason reason) {
        if (reason == null || reason == AccessGateReason.NONE) {
            dismissAccessGate();
            return;
        }
        if (accessGateReason == reason && accessGateDialog != null && accessGateDialog.isShowing()) return;
        dismissAccessGate();
        accessGateReason = reason;

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener((ignored, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);

        FrameLayout full = new FrameLayout(this);
        full.setBackground(makeBg());
        full.setClickable(true);
        full.setFocusable(true);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(24), dp(28), dp(24), dp(26));
        int cardFillColor = lightTheme ? Color.WHITE : Color.rgb(35, 23, 49);
        int cardStrokeColor = lightTheme ? Color.rgb(222, 205, 238) : Color.argb(95, 190, 115, 255);
        card.setBackground(round(cardFillColor, dp(24), cardStrokeColor, 1));
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(12));

        FrameLayout iconWrap = new FrameLayout(this);
        int first = reason == AccessGateReason.OFFLINE ? Color.rgb(255, 142, 70) : Color.rgb(169, 68, 235);
        int second = reason == AccessGateReason.OFFLINE ? Color.rgb(224, 63, 78) : Color.rgb(105, 42, 180);
        iconWrap.setBackground(grad(dp(999), first, second));
        TextView icon = text(reason == AccessGateReason.OFFLINE ? "!" : "×", 38, Color.WHITE, true);
        icon.setTextColor(Color.WHITE);
        icon.setGravity(Gravity.CENTER);
        icon.setIncludeFontPadding(false);
        iconWrap.addView(icon, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));
        card.addView(iconWrap, new LinearLayout.LayoutParams(dp(76), dp(76)));

        int titleRes = reason == AccessGateReason.OFFLINE
                ? R.string.no_internet_title
                : R.string.ad_blocker_title;
        int bodyRes = reason == AccessGateReason.OFFLINE
                ? R.string.no_internet_body
                : R.string.ad_blocker_body;

        TextView title = habboText(t(titleRes), 22, true);
        title.setGravity(Gravity.CENTER);
        title.setIncludeFontPadding(false);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = dp(20);
        card.addView(title, titleLp);

        TextView body = text(t(bodyRes), 14, lightTheme ? Color.rgb(83, 68, 94) : Color.argb(215,255,255,255), false);
        body.setGravity(Gravity.CENTER);
        body.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(-1, -2);
        bodyLp.topMargin = dp(12);
        card.addView(body, bodyLp);

        ProgressBar spinner = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        spinner.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= 21) spinner.setIndeterminateTintList(ColorStateList.valueOf(purple));
        LinearLayout.LayoutParams spinnerLp = new LinearLayout.LayoutParams(dp(30), dp(30));
        spinnerLp.topMargin = dp(22);
        card.addView(spinner, spinnerLp);

        TextView checking = text(t(R.string.access_gate_auto_check), 12, lightTheme ? Color.rgb(100, 82, 112) : Color.argb(175,255,255,255), true);
        checking.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams checkingLp = new LinearLayout.LayoutParams(-1, -2);
        checkingLp.topMargin = dp(8);
        card.addView(checking, checkingLp);

        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        cardLp.leftMargin = dp(24);
        cardLp.rightMargin = dp(24);
        full.addView(card, cardLp);
        dialog.setContentView(full);
        applySafeAreaInsets(dialog.getWindow(), full);
        accessGateDialog = dialog;
        try {
            dialog.show();
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setStatusBarColor(lightTheme ? Color.WHITE : bg);
                window.setNavigationBarColor(lightTheme ? Color.WHITE : bg);
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.MATCH_PARENT;
                params.dimAmount = 0f;
                window.setAttributes(params);
                if (Build.VERSION.SDK_INT >= 23) {
                    int flags = lightTheme ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0;
                    if (Build.VERSION.SDK_INT >= 26 && lightTheme) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                    window.getDecorView().setSystemUiVisibility(flags);
                }
            }
        } catch(Exception ignored) {
            accessGateDialog = null;
        }
    }

    private void dismissAccessGate() {
        accessGateReason = AccessGateReason.NONE;
        if (!billingEntitlementCheckPending && !hasConfirmedAdFreeAccess() && appInForeground) {
            loadInterstitialAd();
            uiHandler.postDelayed(this::maybeShowPendingProfileInterstitial, 120L);
        }
        Dialog dialog = accessGateDialog;
        accessGateDialog = null;
        if (dialog != null) {
            try { dialog.dismiss(); } catch(Exception ignored) {}
        }
    }

    private void buildUi() {
        mainTutorialSettingsTarget = null;
        mainTutorialSearchTarget = null;
        mainTutorialVisualsTarget = null;
        screen = new PullDispatchFrameLayout(this);
        ((PullDispatchFrameLayout) screen).setPullTouchListener(this::handleMainPullToRefreshDispatch);
        screen.setBackground(makeBg());
        ScrollView scroll = new ScrollView(this);
        mainScroll = scroll;
        scroll.setFillViewport(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        scroll.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && searchInput != null && searchInput.hasFocus() && !isTouchInsideView(searchInput, event)) {
                clearSearchFocus();
            }
            return false;
        });
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(10), dp(16), dp(104));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));
        screen.addView(scroll, new FrameLayout.LayoutParams(-1, -1));

        pullRefreshChip = new LinearLayout(this);
        pullRefreshChip.setOrientation(LinearLayout.HORIZONTAL);
        pullRefreshChip.setGravity(Gravity.CENTER_VERTICAL);
        pullRefreshChip.setPadding(dp(14), dp(10), dp(14), dp(10));
        pullRefreshChip.setBackground(round(lightTheme ? Color.WHITE : Color.rgb(36, 24, 54), dp(999), lightTheme ? Color.rgb(216,216,216) : Color.argb(36,255,255,255), 1));
        pullRefreshChip.setAlpha(0f);
        pullRefreshChip.setTranslationY(-dp(40));
        pullRefreshChip.setVisibility(View.GONE);
        pullRefreshSpinner = new CircularPullProgressView(this);
        pullRefreshSpinner.setProgressPct(0);
        pullRefreshChip.addView(pullRefreshSpinner, new LinearLayout.LayoutParams(dp(32), dp(32)));
        pullRefreshText = text(t(R.string.updating_profile), 13, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, true);
        LinearLayout.LayoutParams pullTxtLp = new LinearLayout.LayoutParams(-2, -2);
        pullTxtLp.leftMargin = dp(8);
        pullRefreshChip.addView(pullRefreshText, pullTxtLp);
        FrameLayout.LayoutParams pullLp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        pullLp.topMargin = dp(12);
        screen.addView(pullRefreshChip, pullLp);
        screen.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && searchInput != null && searchInput.hasFocus() && !isTouchInsideView(searchInput, event)) {
                clearSearchFocus();
            }
            return false;
        });
        root.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && searchInput != null && searchInput.hasFocus() && !isTouchInsideView(searchInput, event)) {
                clearSearchFocus();
            }
            return false;
        });

        TextView historyBtn = text("", 22, lightTheme ? Color.rgb(33,33,33) : Color.argb(230,255,255,255), true);
        historyBtn.setGravity(Gravity.CENTER);
        historyBtn.setPadding(0, 0, 0, 0);
        historyBtn.setBackground(new HistoryClockDrawable());
        historyBtn.setOnClickListener(v -> showOpenedProfilesHistoryDialog());
        FrameLayout.LayoutParams historyLp = new FrameLayout.LayoutParams(dp(38), dp(38), Gravity.TOP | Gravity.LEFT);
        historyLp.topMargin = dp(14);
        historyLp.leftMargin = dp(10);
        screen.addView(historyBtn, historyLp);

        rewardAdBtn = text("", 22, Color.WHITE, true);
        rewardAdBtn.setGravity(Gravity.CENTER);
        rewardAdBtn.setPadding(0, 0, 0, 0);
        rewardAdBtn.setIncludeFontPadding(false);
        rewardAdBtn.setBackground(new RewardVideoDrawable());
        rewardAdBtn.setOnClickListener(v -> {
            if (supporterActive) showSponsorProfileDialog();
            else showRewardedAdDialog();
        });
        FrameLayout.LayoutParams rewardLp = new FrameLayout.LayoutParams(dp(38), dp(38), Gravity.TOP | Gravity.RIGHT);
        rewardLp.topMargin = dp(14);
        rewardLp.rightMargin = dp(10);
        screen.addView(rewardAdBtn, rewardLp);

        rewardAdTimeLabel = text("", 9, lightTheme ? Color.rgb(45,45,45) : Color.WHITE, true);
        rewardAdTimeLabel.setGravity(Gravity.CENTER);
        rewardAdTimeLabel.setIncludeFontPadding(false);
        rewardAdTimeLabel.setSingleLine(true);
        rewardAdTimeLabel.setVisibility(View.GONE);
        FrameLayout.LayoutParams rewardTimeLp = new FrameLayout.LayoutParams(dp(58), dp(16), Gravity.TOP | Gravity.RIGHT);
        rewardTimeLp.topMargin = dp(54);
        rewardTimeLp.rightMargin = dp(0);
        screen.addView(rewardAdTimeLabel, rewardTimeLp);

        updateRewardButtonText();
        
        LinearLayout subtitleRow = new LinearLayout(this);
        subtitleRow.setOrientation(LinearLayout.HORIZONTAL);
        subtitleRow.setGravity(Gravity.CENTER);
        root.addView(subtitleRow, lp(-1, dp(42), 48, 0, 48, 18));

        TextView subtitle = text(
                t(R.string.searching),
                19,
                lightTheme ? Color.rgb(34,34,38) : Color.WHITE,
                true
        );
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setLetterSpacing(0.015f);
        subtitleRow.addView(subtitle, new LinearLayout.LayoutParams(-2, -2));

        selectedHotelFlag = new ImageView(this);
        selectedHotelFlag.setImageDrawable(new HotelFlagDrawable(currentHotelKey));
        LinearLayout.LayoutParams selectedFlagLp = new LinearLayout.LayoutParams(dp(28), dp(18));
        selectedFlagLp.leftMargin = dp(8);
        subtitleRow.addView(selectedHotelFlag, selectedFlagLp);

        LinearLayout searchOuter = neutralCard(dp(22));
        searchOuter.setPadding(dp(16), dp(16), dp(16), dp(16));
        if (Build.VERSION.SDK_INT >= 21) searchOuter.setElevation(dp(5));
        root.addView(searchOuter, lp(-1, -2, 0, 2, 0, 12));
        mainTutorialSearchTarget = searchOuter;

        LinearLayout searchCard = neutralCard(dp(18));
        searchCard.setBackgroundColor(Color.TRANSPARENT);
        searchCard.setElevation(0f);
        searchCard.setPadding(0, 0, 0, 0);
        searchOuter.addView(searchCard, lp(-1, -2, 0, 0, 0, 0));

        searchInput = new EditText(this);
        searchInput.setSingleLine(true);
        searchInput.setHint(t(R.string.search_hint));
        searchInput.setHintTextColor(lightTheme ? Color.rgb(117, 117, 117) : Color.argb(135,255,255,255));
        searchInput.setTextColor(lightTheme ? Color.rgb(33, 33, 33) : Color.WHITE);
        searchInput.setTextSize(16);
        searchInput.setTypeface(habboFont);
        searchInput.setGravity(Gravity.CENTER_VERTICAL);
        searchInput.setPadding(dp(16), 0, dp(16), 0);
        searchInput.setBackground(round(
                lightTheme ? Color.rgb(247,247,249) : Color.rgb(13,14,21),
                dp(16),
                lightTheme ? Color.rgb(214,214,221) : Color.rgb(57,52,73),
                1
        ));
        searchInput.setCursorVisible(false);
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            searchInput.setCursorVisible(hasFocus);
            if (!hasFocus) setSuggestionsVisible(false);
        });
        searchCard.addView(searchInput, lp(-1, dp(52), 0, 0, 0, 12));

        suggestionsScroll = new ScrollView(this);
        suggestionsScroll.setVisibility(View.GONE);
        suggestionsScroll.setFillViewport(false);
        suggestionsScroll.setVerticalScrollBarEnabled(true);
        suggestionsScroll.setScrollbarFadingEnabled(false);
        suggestionsScroll.setNestedScrollingEnabled(true);
        suggestionsScroll.setOnTouchListener((v, event) -> {
            requestDisallowParents(v, true);
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) requestDisallowParents(v, false);
            return false;
        });
        tintScrollBar(suggestionsScroll);

        suggestionsBox = new LinearLayout(this);
        suggestionsBox.setOrientation(LinearLayout.VERTICAL);
        suggestionsScroll.addView(suggestionsBox, new ScrollView.LayoutParams(-1, -2));

        searchCard.addView(suggestionsScroll, lp(-1, dp(230), 0, 0, 0, 10));

        searchBtn = new Button(this);
        searchBtn.setText(t(R.string.search_button));
        searchBtn.setTextColor(Color.WHITE);
        searchBtn.setTextSize(16);
        searchBtn.setAllCaps(false);
        searchBtn.setTypeface(Typeface.DEFAULT_BOLD);
        searchBtn.setLetterSpacing(0.02f);
        searchBtn.setBackground(grad(dp(16), purple2, purple));
        if (Build.VERSION.SDK_INT >= 21) searchBtn.setElevation(dp(3));
        searchCard.addView(searchBtn, lp(-1, dp(54), 0, 0, 0, 0));

        sponsorsSection = buildSponsorsSection();
        root.addView(sponsorsSection, lp(-1, -2, 0, 4, 0, 14));

        startNativeAdContainer = new FrameLayout(this);
        startNativeAdContainer.setVisibility(View.GONE);
        root.addView(startNativeAdContainer, lp(-1, -2, 0, 0, 0, 16));
        if (startNativeAd != null) renderStartNativeAd();

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        progress.setVisibility(View.GONE);
        root.addView(progress, lp(-1, dp(34), 0, 0, 0, 2));
        statusText = text("", 14, Color.argb(210,255,255,255), false);
        statusText.setGravity(Gravity.CENTER);
        statusText.setVisibility(View.GONE);
        root.addView(statusText, lp(-1, -2, 0, 0, 0, 0));

        resultWrap = new LinearLayout(this);
        resultWrap.setOrientation(LinearLayout.VERTICAL);
        root.addView(resultWrap, lp(-1, -2, 0, 0, 0, 0));
        setContentView(screen);
        applySafeAreaInsets(getWindow(), screen);
        searchBtn.setOnClickListener(v -> {
            setSuggestionsVisible(false);
            clearSearchFocus();
            search();
        });
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            setSuggestionsVisible(false);
            clearSearchFocus();
            search();
            return true;
        });
        bindNickSuggestions();
        showStartState();
        if (!openingSplashShownThisSession) {
            showOpeningSplashOverlay();
        } else {
            bindBottomNavigationAutoHide(
                    mainScroll,
                    addBottomNavigation(screen, 0, null)
            );
        }
        maybeShowFirstRunTutorial();
        if (habbodexWebView != null) attachHabbodexWebViewHidden();
    }

    private LinearLayout buildSponsorsSection() {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(0, dp(3), 0, 0);
        section.setBackgroundColor(Color.TRANSPARENT);
        section.setClipChildren(false);
        section.setClipToPadding(false);

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.HORIZONTAL);
        heading.setGravity(Gravity.CENTER);
        section.addView(heading, lp(-1, dp(36), 2, 0, 2, 2));

        TextView sparkle = text("✦", 16, pink, true);
        sparkle.setGravity(Gravity.CENTER);
        sparkle.setIncludeFontPadding(false);
        heading.addView(sparkle, new LinearLayout.LayoutParams(dp(25), dp(30)));

        TextView title = text(t(R.string.sponsors_title), 18, lightTheme ? Color.rgb(56, 35, 70) : Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        title.setLetterSpacing(0.01f);
        heading.addView(title, new LinearLayout.LayoutParams(-2, -1));

        sponsorsCarouselHost = new FrameLayout(this);
        sponsorsCarouselHost.setClipChildren(false);
        sponsorsCarouselHost.setClipToPadding(false);
        section.addView(sponsorsCarouselHost, lp(-1, dp(106), 0, 0, 0, 0));

        HorizontalScrollView carousel = new HorizontalScrollView(this);
        sponsorsCarouselScroll = carousel;
        carousel.setHorizontalScrollBarEnabled(false);
        carousel.setFillViewport(false);
        carousel.setClipChildren(false);
        carousel.setClipToPadding(false);
        carousel.setHorizontalFadingEdgeEnabled(true);
        carousel.setFadingEdgeLength(dp(22));
        sponsorsCarouselRow = new LinearLayout(this);
        sponsorsCarouselRow.setOrientation(LinearLayout.HORIZONTAL);
        sponsorsCarouselRow.setGravity(Gravity.CENTER_VERTICAL);
        sponsorsCarouselRow.setPadding(dp(1), 0, dp(18), 0);
        carousel.addView(sponsorsCarouselRow, new HorizontalScrollView.LayoutParams(-2, -1));
        sponsorsCarouselHost.addView(carousel, new FrameLayout.LayoutParams(-1, -1));

        sponsorsLoadingIndicator = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        sponsorsLoadingIndicator.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= 21) {
            sponsorsLoadingIndicator.setIndeterminateTintList(ColorStateList.valueOf(purple));
        }
        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(dp(30), dp(30), Gravity.CENTER);
        sponsorsCarouselHost.addView(sponsorsLoadingIndicator, spinnerParams);

        String cached = sponsorsCacheJson;
        if (cached == null) {
            setSponsorsLoadingVisible(true);
        } else {
            try {
                renderSponsors(new JSONArray(cached));
            } catch(Exception ignored) {
                sponsorsCacheJson = null;
                setSponsorsLoadingVisible(true);
            }
        }
        return section;
    }

    private void setSponsorsLoadingVisible(boolean visible) {
        if (sponsorsLoadingIndicator == null) return;
        if (visible && sponsorsCarouselHost != null) sponsorsCarouselHost.setVisibility(View.VISIBLE);
        sponsorsLoadingIndicator.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void finishSponsorsDisplay(boolean hasSponsors) {
        setSponsorsLoadingVisible(false);
        if (sponsorsCarouselHost != null) {
            sponsorsCarouselHost.setVisibility(hasSponsors ? View.VISIBLE : View.GONE);
        }
    }

    private void updateSponsorsSubscribeButton() {
        if (sponsorsSubscribeButton == null) return;
        if (sponsorsActionIcon != null) {
            sponsorsActionIcon.setText(supporterActive ? "✦" : "+");
            sponsorsActionIcon.setTextSize(supporterActive ? 26 : 34);
        }
        if (sponsorsActionGlow != null) sponsorsActionGlow.invalidate();
        sponsorsSubscribeButton.setContentDescription(t(supporterActive
                ? R.string.supporter_manage
                : R.string.supporter_subscribe));
    }

    private void refreshSponsors() {
        if (sponsorsLoading) {
            runOnUiThread(() -> {
                if (sponsorsCacheJson == null
                        && (sponsorsCarouselRow == null || sponsorsCarouselRow.getChildCount() == 0)) {
                    setSponsorsLoadingVisible(true);
                }
            });
            return;
        }
        sponsorsLoading = true;
        runOnUiThread(() -> {
            if (sponsorsCarouselRow == null || sponsorsCarouselRow.getChildCount() == 0) {
                setSponsorsLoadingVisible(true);
            }
        });
        executor.execute(() -> {
            try {
                JSONObject response = getJson(PROFILE_API + "/sponsors?limit=100");
                JSONArray sponsors = response.optJSONArray("sponsors");
                if (sponsors == null) sponsors = response.optJSONArray("items");
                final JSONArray finalSponsors = sponsors == null ? new JSONArray() : sponsors;
                sponsorsCacheJson = finalSponsors.toString();
                runOnUiThread(() -> renderSponsors(finalSponsors));
            } catch(Exception error) {
                runOnUiThread(() -> {
                    if (sponsorsCarouselRow != null && sponsorsCarouselRow.getChildCount() == 0) {
                        sponsorsCarouselRow.addView(sponsorActionCard());
                    }
                    finishSponsorsDisplay(
                            sponsorsCarouselRow != null && sponsorsCarouselRow.getChildCount() > 0
                    );
                });
            } finally {
                sponsorsLoading = false;
            }
        });
    }

    private void renderSponsors(JSONArray sponsors) {
        if (sponsorsCarouselRow == null) return;
        sponsorsCarouselRow.removeAllViews();
        sponsorsSubscribeButton = null;
        sponsorsActionIcon = null;
        sponsorsActionGlow = null;
        for (int i = 0; i < sponsors.length(); i++) {
            JSONObject sponsor = sponsors.optJSONObject(i);
            if (sponsor == null) continue;
            String nick = sponsor.optString("nick", sponsor.optString("name", "")).trim();
            String hotel = normalizeHotelKey(sponsor.optString("hotel", "br"));
            String figure = sponsor.optString("figure", sponsor.optString("figureString", "")).trim();
            String uniqueId = sponsor.optString("uniqueId", sponsor.optString("id", "")).trim();
            if (nick.isEmpty() || figure.isEmpty()) continue;
            sponsorsCarouselRow.addView(sponsorCard(nick, hotel, figure, uniqueId));
        }
        // O convite para assinar faz parte do próprio carrossel e permanece
        // sempre por último, com o mesmo formato visual dos patrocinadores.
        sponsorsCarouselRow.addView(sponsorActionCard());
        finishSponsorsDisplay(sponsorsCarouselRow.getChildCount() > 0);
    }

    private View sponsorCard(String nick, String hotel, String figure, String uniqueId) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        item.setPadding(dp(2), 0, dp(2), 0);
        item.setBackgroundColor(Color.TRANSPARENT);
        item.setClipChildren(false);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(dp(90), dp(104));
        itemParams.rightMargin = dp(5);
        item.setLayoutParams(itemParams);

        FrameLayout avatarHost = new FrameLayout(this);
        avatarHost.setClipChildren(false);
        avatarHost.setClipToPadding(false);
        item.addView(avatarHost, new LinearLayout.LayoutParams(dp(82), dp(80)));

        SponsorHeadGlowView glow = new SponsorHeadGlowView(this);
        FrameLayout.LayoutParams glowParams = new FrameLayout.LayoutParams(dp(74), dp(74), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        avatarHost.addView(glow, glowParams);

        ImageView head = new ImageView(this);
        head.setScaleType(ImageView.ScaleType.FIT_CENTER);
        head.setPadding(dp(3), dp(2), dp(3), dp(2));
        String headUrl = "https://" + hotelDomain(hotel)
                + "/habbo-imaging/avatarimage?figure=" + enc(figure)
                + "&size=m&direction=2&head_direction=2&headonly=1";
        loadHeadImage(head, headUrl);
        FrameLayout.LayoutParams headParams = new FrameLayout.LayoutParams(dp(70), dp(70), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        headParams.bottomMargin = dp(2);
        avatarHost.addView(head, headParams);

        ImageView flag = new ImageView(this);
        flag.setImageDrawable(new HotelFlagDrawable(hotel, false));
        if (Build.VERSION.SDK_INT >= 21) flag.setElevation(dp(8));
        FrameLayout.LayoutParams flagParams = new FrameLayout.LayoutParams(dp(25), dp(17), Gravity.TOP | Gravity.RIGHT);
        flagParams.topMargin = dp(2);
        flagParams.rightMargin = dp(1);
        avatarHost.addView(flag, flagParams);

        TextView name = habboText(nick, 12, true);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        name.setGravity(Gravity.CENTER);
        name.setIncludeFontPadding(false);
        name.setTextColor(lightTheme ? Color.rgb(39, 31, 47) : Color.WHITE);
        item.addView(name, new LinearLayout.LayoutParams(dp(86), dp(23)));

        item.setContentDescription(nick + " - " + hotel.toUpperCase(Locale.ROOT));
        item.setOnClickListener(v -> openSponsorProfile(nick, uniqueId, figure, hotel));
        return item;
    }

    private View sponsorActionCard() {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        item.setPadding(dp(2), 0, dp(2), 0);
        item.setBackgroundColor(Color.TRANSPARENT);
        item.setClipChildren(false);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(dp(90), dp(104));
        itemParams.rightMargin = dp(5);
        item.setLayoutParams(itemParams);

        FrameLayout avatarHost = new FrameLayout(this);
        avatarHost.setClipChildren(false);
        avatarHost.setClipToPadding(false);
        item.addView(avatarHost, new LinearLayout.LayoutParams(dp(82), dp(80)));

        SponsorHeadGlowView glow = new SponsorHeadGlowView(this);
        sponsorsActionGlow = glow;
        FrameLayout.LayoutParams glowParams = new FrameLayout.LayoutParams(dp(74), dp(74), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        avatarHost.addView(glow, glowParams);

        sponsorsActionIcon = text(supporterActive ? "✦" : "+", supporterActive ? 26 : 34, Color.WHITE, true);
        sponsorsActionIcon.setGravity(Gravity.CENTER);
        sponsorsActionIcon.setIncludeFontPadding(false);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(dp(70), dp(70), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        iconParams.bottomMargin = dp(2);
        avatarHost.addView(sponsorsActionIcon, iconParams);

        sponsorsSubscribeButton = item;
        updateSponsorsSubscribeButton();
        item.setOnClickListener(v -> {
            if (supporterActive) showSupporterManageDialog();
            else showSupporterOfferDialog();
        });
        return item;
    }

    private void showSupporterOfferDialog() {
        if (supporterActive) {
            showSupporterManageDialog();
            return;
        }
        final Dialog dialog = new Dialog(this);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(20), dp(20), dp(20), dp(20));
        wrap.setBackground(round(dialogFillColor(), dp(24), dialogStrokeColor(), 1));
        dialog.setContentView(wrap);
        applySafeAreaInsets(dialog.getWindow(), wrap);

        TextView title = toxicLogoText(t(R.string.supporter_title), 22);
        title.setGravity(Gravity.CENTER);
        wrap.addView(title, lp(-1, -2, 0, 0, 0, 10));
        TextView body = text(t(R.string.supporter_offer_body), 14, lightTheme ? Color.rgb(61, 52, 69) : Color.argb(226,255,255,255), false);
        body.setGravity(Gravity.CENTER);
        body.setLineSpacing(dp(3), 1f);
        wrap.addView(body, lp(-1, -2, 4, 0, 4, 12));

        String price = supporterPriceText();
        TextView terms = text(price.isEmpty()
                        ? t(R.string.supporter_recurring_terms)
                        : tr(R.string.supporter_price_terms, price),
                12, themeMutedColor(), false);
        terms.setGravity(Gravity.CENTER);
        terms.setLineSpacing(dp(2), 1f);
        terms.setPadding(dp(10), dp(10), dp(10), dp(10));
        terms.setBackground(round(lightTheme ? Color.rgb(246,244,249) : Color.argb(20,255,255,255), dp(14), dialogStrokeColor(), 1));
        wrap.addView(terms, lp(-1, -2, 0, 0, 0, 14));

        TextView subscribe = dialogButton(t(R.string.supporter_subscribe));
        subscribe.setTextColor(Color.WHITE);
        subscribe.setBackground(grad(dp(15), purple2, purple));
        wrap.addView(subscribe, lp(-1, dp(50), 0, 0, 0, 8));
        subscribe.setOnClickListener(v -> {
            dialog.dismiss();
            launchSupporterPurchase();
        });
        TextView cancel = dialogButton(t(R.string.cancel));
        cancel.setTextColor(lightTheme ? Color.rgb(45,45,45) : Color.WHITE);
        cancel.setBackground(round(lightTheme ? Color.rgb(242,242,244) : Color.argb(18,255,255,255), dp(14), dialogStrokeColor(), 1));
        wrap.addView(cancel, lp(-1, dp(46), 0, 0, 0, 0));
        cancel.setOnClickListener(v -> dialog.dismiss());
        showCompactDialog(dialog, dp(430));
    }

    private void showSupporterManageDialog() {
        final Dialog dialog = new Dialog(this);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(20), dp(20), dp(20), dp(20));
        wrap.setBackground(round(dialogFillColor(), dp(24), dialogStrokeColor(), 1));
        dialog.setContentView(wrap);
        applySafeAreaInsets(dialog.getWindow(), wrap);
        TextView title = toxicLogoText(t(R.string.supporter_active_title), 21);
        title.setGravity(Gravity.CENTER);
        wrap.addView(title, lp(-1, -2, 0, 0, 0, 10));
        TextView body = text(t(R.string.supporter_active_body), 14, themeMutedColor(), false);
        body.setGravity(Gravity.CENTER);
        body.setLineSpacing(dp(3), 1f);
        wrap.addView(body, lp(-1, -2, 0, 0, 0, 14));

        TextView choose = dialogButton(t(R.string.supporter_choose_profile));
        choose.setTextColor(Color.WHITE);
        choose.setBackground(grad(dp(15), purple2, purple));
        wrap.addView(choose, lp(-1, dp(50), 0, 0, 0, 8));
        choose.setOnClickListener(v -> {
            dialog.dismiss();
            showSponsorProfileDialog();
        });
        TextView manage = dialogButton(t(R.string.supporter_manage_google));
        manage.setTextColor(lightTheme ? Color.rgb(45,45,45) : Color.WHITE);
        manage.setBackground(round(lightTheme ? Color.rgb(242,242,244) : Color.argb(18,255,255,255), dp(14), dialogStrokeColor(), 1));
        wrap.addView(manage, lp(-1, dp(48), 0, 0, 0, 8));
        manage.setOnClickListener(v -> openSupporterManagement());
        TextView close = dialogButton(t(R.string.close));
        close.setTextColor(themeMutedColor());
        wrap.addView(close, lp(-1, dp(44), 0, 0, 0, 0));
        close.setOnClickListener(v -> dialog.dismiss());
        showCompactDialog(dialog, dp(430));
    }

    private void showCompactDialog(Dialog dialog, int maxWidth) {
        dialog.show();
        Window window = dialog.getWindow();
        if (window == null) return;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(window.getAttributes());
        params.width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(28), maxWidth);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }

    private void openSupporterManagement() {
        try {
            String url = "https://play.google.com/store/account/subscriptions?sku="
                    + enc(SUPPORTER_PRODUCT_ID)
                    + "&package=" + enc(getPackageName());
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch(Exception ignored) {
            toast(t(R.string.purchase_error));
        }
    }

    private void showSponsorProfileDialog() {
        if (!supporterActive || supporterPurchaseToken.isEmpty()) {
            toast(t(R.string.supporter_validation_pending));
            querySupporterPurchases();
            return;
        }
        final Dialog dialog = new Dialog(this);
        ScrollView scroll = new ScrollView(this);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(20), dp(20), dp(20), dp(20));
        wrap.setBackground(round(dialogFillColor(), dp(24), dialogStrokeColor(), 1));
        scroll.addView(wrap, new ScrollView.LayoutParams(-1, -2));
        dialog.setContentView(scroll);
        applySafeAreaInsets(dialog.getWindow(), scroll);

        TextView title = toxicLogoText(t(R.string.supporter_choose_profile), 21);
        title.setGravity(Gravity.CENTER);
        wrap.addView(title, lp(-1, -2, 0, 0, 0, 8));
        TextView body = text(t(R.string.supporter_profile_body), 13, themeMutedColor(), false);
        body.setGravity(Gravity.CENTER);
        body.setLineSpacing(dp(2), 1f);
        wrap.addView(body, lp(-1, -2, 0, 0, 0, 12));

        EditText nickInput = new EditText(this);
        String initialNick = activeRenderedProfile != null && activeRenderedProfile.name != null
                ? activeRenderedProfile.name
                : supporterProfileNick;
        nickInput.setText(initialNick == null ? "" : initialNick);
        nickInput.setHint(t(R.string.search_hint));
        nickInput.setSingleLine(true);
        nickInput.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
        nickInput.setHintTextColor(themeMutedColor());
        nickInput.setTextSize(16);
        nickInput.setTypeface(habboFont);
        nickInput.setPadding(dp(14), 0, dp(14), 0);
        nickInput.setBackground(round(lightTheme ? Color.rgb(247,247,249) : Color.rgb(13,14,21), dp(15), dialogStrokeColor(), 1));
        wrap.addView(nickInput, lp(-1, dp(52), 0, 0, 0, 12));

        String initialHotel = activeRenderedProfile != null
                ? normalizeHotelKey(activeRenderedProfile.hotelKey)
                : normalizeHotelKey(supporterProfileHotel);
        if (initialHotel.isEmpty()) initialHotel = currentHotelKey;
        final String[] selectedHotel = {initialHotel};
        LinearLayout hotelGrid = new LinearLayout(this);
        hotelGrid.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(hotelGrid, lp(-1, -2, 0, 0, 0, 12));
        rebuildSponsorHotelGrid(hotelGrid, selectedHotel);

        TextView cooldown = text("", 12, themeMutedColor(), false);
        cooldown.setGravity(Gravity.CENTER);
        long wait = Math.max(0L, supporterCanChangeAtMs - System.currentTimeMillis());
        if (wait > 0L) {
            cooldown.setText(tr(R.string.supporter_change_wait, formatSupporterCooldown(wait)));
            cooldown.setVisibility(View.VISIBLE);
        } else {
            cooldown.setVisibility(View.GONE);
        }
        wrap.addView(cooldown, lp(-1, -2, 0, 0, 0, 10));

        TextView save = dialogButton(t(R.string.supporter_save_profile));
        save.setTextColor(Color.WHITE);
        save.setBackground(grad(dp(15), purple2, purple));
        save.setEnabled(wait <= 0L);
        save.setAlpha(wait > 0L ? 0.5f : 1f);
        wrap.addView(save, lp(-1, dp(50), 0, 0, 0, 8));
        save.setOnClickListener(v -> {
            String nick = nickInput.getText().toString().trim();
            if (nick.isEmpty()) {
                toast(t(R.string.type_nick_toast));
                return;
            }
            save.setEnabled(false);
            save.setText(t(R.string.supporter_saving));
            submitSponsorProfile(dialog, save, nick, selectedHotel[0]);
        });
        TextView cancel = dialogButton(t(R.string.cancel));
        cancel.setTextColor(themeMutedColor());
        wrap.addView(cancel, lp(-1, dp(44), 0, 0, 0, 0));
        cancel.setOnClickListener(v -> dialog.dismiss());
        showCompactDialog(dialog, dp(440));
    }

    private void rebuildSponsorHotelGrid(LinearLayout grid, String[] selectedHotel) {
        grid.removeAllViews();
        addSponsorHotelChoiceRow(grid, selectedHotel, "br", "com", "es");
        addSponsorHotelChoiceRow(grid, selectedHotel, "de", "fr", "fi");
        addSponsorHotelChoiceRow(grid, selectedHotel, "it", "nl", "tr");
    }

    private void addSponsorHotelChoiceRow(LinearLayout grid, String[] selectedHotel, String a, String b, String c) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        grid.addView(row, lp(-1, dp(44), 0, 0, 0, 7));
        addSponsorHotelChoice(row, grid, selectedHotel, a, 0);
        addSponsorHotelChoice(row, grid, selectedHotel, b, 1);
        addSponsorHotelChoice(row, grid, selectedHotel, c, 2);
    }

    private void addSponsorHotelChoice(LinearLayout row, LinearLayout grid, String[] selectedHotel, String hotel, int position) {
        ImageView button = new ImageView(this);
        button.setPadding(dp(12), dp(9), dp(12), dp(9));
        button.setImageDrawable(new HotelFlagDrawable(hotel));
        button.setBackground(hotel.equals(selectedHotel[0])
                ? grad(dp(12), purple2, purple)
                : round(lightTheme ? Color.rgb(248,248,250) : Color.argb(18,255,255,255), dp(12), dialogStrokeColor(), 1));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(42), 1);
        if (position > 0) params.leftMargin = dp(6);
        row.addView(button, params);
        button.setOnClickListener(v -> {
            selectedHotel[0] = hotel;
            rebuildSponsorHotelGrid(grid, selectedHotel);
        });
    }

    private void submitSponsorProfile(Dialog dialog, TextView saveButton, String nick, String hotel) {
        final String token = supporterPurchaseToken;
        executor.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("purchaseToken", token);
                payload.put("nick", nick);
                payload.put("hotel", normalizeHotelKey(hotel));
                JSONObject response = postJsonObject(PROFILE_API + "/sponsors/profile", payload);
                applySupporterStatus(response);
                runOnUiThread(() -> {
                    dialog.dismiss();
                    toast(t(R.string.supporter_profile_saved));
                    refreshSponsors();
                });
            } catch(ApiHttpException error) {
                JSONObject body = error.payload;
                String next = body.optString("nextChangeAt", "");
                Date nextDate = parseHabboDate(next);
                if (nextDate != null) supporterCanChangeAtMs = nextDate.getTime();
                final String message = "profile_change_cooldown".equals(body.optString("code"))
                        ? tr(R.string.supporter_change_wait, formatSupporterCooldown(Math.max(0L, supporterCanChangeAtMs - System.currentTimeMillis())))
                        : body.optString("error", t(R.string.supporter_save_error));
                runOnUiThread(() -> {
                    saveButton.setEnabled(true);
                    saveButton.setText(t(R.string.supporter_save_profile));
                    toast(message);
                });
            } catch(Exception error) {
                runOnUiThread(() -> {
                    saveButton.setEnabled(true);
                    saveButton.setText(t(R.string.supporter_save_profile));
                    toast(t(R.string.supporter_save_error));
                });
            }
        });
    }

    private String formatSupporterCooldown(long millis) {
        long totalMinutes = Math.max(1L, (long)Math.ceil(millis / 60000.0));
        long hours = totalMinutes / 60L;
        long minutes = totalMinutes % 60L;
        if (hours > 0L) return tr(R.string.duration_short_hours, hours, minutes);
        return totalMinutes + " min";
    }

    private void syncSupporterStatusWithBackend(String purchaseToken, boolean showActivation) {
        if (purchaseToken == null || purchaseToken.trim().isEmpty() || supporterStatusRequestRunning) return;
        supporterStatusRequestRunning = true;
        executor.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("purchaseToken", purchaseToken);
                JSONObject response = postJsonObject(PROFILE_API + "/sponsors/status", payload);
                boolean active = response.optBoolean("active", false);
                applySupporterStatus(response);
                supporterNextVerificationAtMs = System.currentTimeMillis() + SUPPORTER_REVERIFY_INTERVAL_MS;
                runOnUiThread(() -> {
                    // A compra local apenas fornece o token. O direito e a
                    // remoção de anúncios dependem desta confirmação segura.
                    finishBillingEntitlementCheck(active);
                    if (active && showActivation) toast(t(R.string.supporter_activated));
                    refreshSponsors();
                    if (active) uiHandler.postDelayed(this::maybeShowSupporterTutorial, 650L);
                });
            } catch(ApiHttpException error) {
                supporterNextVerificationAtMs = System.currentTimeMillis() + 2L * 60L * 1000L;
                if (error.statusCode == 401 || error.statusCode == 403 || error.statusCode == 410) {
                    runOnUiThread(() -> finishBillingEntitlementCheck(false));
                } else {
                    runOnUiThread(() -> {
                        // Em uma falha temporária, conserva somente um direito
                        // que já havia sido confirmado nesta execução.
                        finishBillingEntitlementCheck(supporterActive);
                        if (showActivation) toast(t(R.string.supporter_server_unavailable));
                    });
                }
            } catch(Exception error) {
                supporterNextVerificationAtMs = System.currentTimeMillis() + 2L * 60L * 1000L;
                runOnUiThread(() -> {
                    finishBillingEntitlementCheck(supporterActive);
                    if (showActivation) toast(t(R.string.supporter_server_unavailable));
                });
            } finally {
                supporterStatusRequestRunning = false;
            }
        });
    }

    private void applySupporterStatus(JSONObject response) {
        if (response == null) return;
        Date nextDate = parseHabboDate(response.optString("canChangeAt", response.optString("nextChangeAt", "")));
        supporterCanChangeAtMs = nextDate == null ? 0L : nextDate.getTime();
        Date expiryDate = parseHabboDate(response.optString("expiresAt", ""));
        supporterExpiresAtMs = expiryDate == null ? 0L : expiryDate.getTime();
        JSONObject sponsor = response.optJSONObject("sponsor");
        if (sponsor != null) {
            supporterProfileNick = sponsor.optString("nick", "");
            supporterProfileHotel = normalizeHotelKey(sponsor.optString("hotel", ""));
        }
    }

    private void maybeShowSupporterTutorial() {
        android.content.SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean pending = preferences.getBoolean(PREF_SUPPORTER_TUTORIAL_PENDING, false);
        int shownVersion = preferences.getInt(PREF_SUPPORTER_TUTORIAL_VERSION, 0);
        if (!supporterActive || (!pending && shownVersion >= CURRENT_SUPPORTER_TUTORIAL_VERSION)) return;
        if (tutorialOverlayView != null || sponsorsSubscribeButton == null || sponsorsSubscribeButton.getWindowToken() == null) {
            uiHandler.postDelayed(this::maybeShowSupporterTutorial, 450L);
            return;
        }
        Rect visibleTarget = new Rect();
        boolean mostlyVisible = sponsorsSubscribeButton.getGlobalVisibleRect(visibleTarget)
                && visibleTarget.height() >= sponsorsSubscribeButton.getHeight() * .72f;
        if (!mostlyVisible && mainScroll != null && sponsorsSection != null) {
            mainScroll.smoothScrollTo(0, Math.max(0, sponsorsSection.getTop() - dp(76)));
            if (sponsorsCarouselScroll != null) sponsorsCarouselScroll.fullScroll(View.FOCUS_RIGHT);
            uiHandler.postDelayed(this::maybeShowSupporterTutorial, 360L);
            return;
        }
        cancelTutorialPulseAnimation();
        FrameLayout overlay = new FrameLayout(this);
        overlay.setClickable(true);
        overlay.setFocusable(true);
        tutorialOverlayView = overlay;
        ProfileTutorialOverlayDrawable drawable = new ProfileTutorialOverlayDrawable(
                overlay,
                sponsorsSubscribeButton,
                8,
                0
        );
        overlay.setBackground(drawable);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackground(new TutorialCardDrawable(0));
        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        cardParams.leftMargin = dp(22);
        cardParams.rightMargin = dp(22);
        cardParams.bottomMargin = dp(92);
        overlay.addView(card, cardParams);

        TextView title = habboText(t(R.string.supporter_tutorial_title), 20, true);
        title.setGravity(Gravity.CENTER);
        card.addView(title, lp(-1, -2, 0, 0, 0, 8));
        TextView body = text(t(R.string.supporter_tutorial_body), 14, Color.argb(225,255,255,255), false);
        body.setGravity(Gravity.CENTER);
        body.setLineSpacing(dp(3), 1f);
        card.addView(body, lp(-1, -2, 0, 0, 0, 12));
        TextView choose = dialogButton(t(R.string.supporter_choose_now));
        choose.setTextColor(Color.WHITE);
        choose.setBackground(grad(dp(14), purple2, purple));
        card.addView(choose, lp(-1, dp(48), 0, 0, 0, 0));

        Runnable finish = () -> {
            cancelTutorialPulseAnimation();
            detachViewFromParent(overlay);
            if (tutorialOverlayView == overlay) tutorialOverlayView = null;
            preferences.edit()
                    .putBoolean(PREF_SUPPORTER_TUTORIAL_PENDING, false)
                    .putInt(PREF_SUPPORTER_TUTORIAL_VERSION, CURRENT_SUPPORTER_TUTORIAL_VERSION)
                    .apply();
            showSponsorProfileDialog();
        };
        choose.setOnClickListener(v -> finish.run());
        screen.addView(overlay, new FrameLayout.LayoutParams(-1, -1));

        ValueAnimator pulse = ValueAnimator.ofFloat(0f, 1f);
        tutorialPulseAnimator = pulse;
        pulse.setDuration(1150L);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setRepeatMode(ValueAnimator.REVERSE);
        pulse.addUpdateListener(animation -> {
            drawable.setPulse((Float)animation.getAnimatedValue());
            overlay.invalidate();
        });
        pulse.start();
    }

    private void showOpeningSplashOverlay() {
        if (screen == null) return;
        openingSplashShownThisSession = true;

        final FrameLayout splash = new FrameLayout(this);
        splash.setBackgroundColor(Color.BLACK);
        splash.setClickable(true);
        splash.setFocusable(true);

        LinearLayout splashCenter = new LinearLayout(this);
        splashCenter.setOrientation(LinearLayout.VERTICAL);
        splashCenter.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams centerLp = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        splash.addView(splashCenter, centerLp);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.round_launcher);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setPadding(dp(8), dp(8), dp(8), dp(8));
        splashCenter.addView(logo, new LinearLayout.LayoutParams(-1, dp(320)));

        LinearLayout disclaimerWrap = new LinearLayout(this);
        disclaimerWrap.setOrientation(LinearLayout.VERTICAL);
        disclaimerWrap.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams disclaimerLp = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        disclaimerLp.bottomMargin = dp(10);
        splash.addView(disclaimerWrap, disclaimerLp);

        TextView disclaimer1 = text(t(R.string.disclaimer1), 12, Color.argb(210,255,255,255), false);
        disclaimer1.setGravity(Gravity.CENTER);
        disclaimer1.setLineSpacing(dp(2), 1f);
        disclaimer1.setPadding(dp(26), dp(4), dp(26), 0);
        disclaimerWrap.addView(disclaimer1, new LinearLayout.LayoutParams(-1, -2));


        screen.addView(splash, new FrameLayout.LayoutParams(-1, -1));
        splash.bringToFront();

        uiHandler.postDelayed(() -> {
            splash.animate()
                    .alpha(0f)
                    .setDuration(260)
                    .withEndAction(() -> {
                        try { screen.removeView(splash); } catch (Exception ignored) {}
                        bindBottomNavigationAutoHide(
                                mainScroll,
                                addBottomNavigation(screen, 0, null)
                        );
                    })
                    .start();
        }, 2000L);
    }


    private void maybeShowFirstRunTutorial() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (sp.getInt(PREF_TUTORIAL_VERSION, 0) >= CURRENT_TUTORIAL_VERSION) return;
        sp.edit().putInt(PREF_TUTORIAL_VERSION, CURRENT_TUTORIAL_VERSION).apply();
        uiHandler.postDelayed(() -> showTutorialOverlay(0), 2300L);
    }

    private int tutorialAccentColor(int step) {
        return Color.rgb(190, 96, 255);
    }

    private int tutorialAccentSecondaryColor(int step) {
        return Color.rgb(104, 48, 196);
    }

    private void cancelTutorialPulseAnimation() {
        if (tutorialPulseAnimator != null) {
            try { tutorialPulseAnimator.cancel(); } catch (Exception ignored) {}
            tutorialPulseAnimator = null;
        }
    }

    private void showTutorialOverlay(final int step) {
        if (screen == null) return;
        final int safeStep = Math.max(0, Math.min(2, step));
        final View target = safeStep == 0
                ? mainTutorialSettingsTarget
                : (safeStep == 1 ? mainTutorialSearchTarget : mainTutorialVisualsTarget);
        final int targetPadding = safeStep == 1 ? 7 : 6;
        if (
                target == null
                || target.getParent() == null
                || target.getWidth() <= 0
                || target.getHeight() <= 0
                || tutorialTargetBounds(screen, target, targetPadding) == null
        ) {
            uiHandler.postDelayed(() -> showTutorialOverlay(safeStep), 180L);
            return;
        }
        cancelTutorialPulseAnimation();
        if (tutorialOverlayView != null) detachViewFromParent(tutorialOverlayView);

        final FrameLayout overlay = new FrameLayout(this);
        tutorialOverlayView = overlay;
        if (Build.VERSION.SDK_INT >= 21) overlay.setElevation(dp(80));
        overlay.setClickable(true);
        overlay.setFocusable(true);
        final TutorialOverlayDrawable overlayDrawable = new TutorialOverlayDrawable(
                overlay,
                target,
                targetPadding,
                safeStep
        );
        overlay.setBackground(overlayDrawable);

        final LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(16));
        card.setBackground(new TutorialCardDrawable(safeStep));
        card.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(36));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(header, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams headingLp = new LinearLayout.LayoutParams(-1, -2);
        header.addView(heading, headingLp);

        int accent = tutorialAccentColor(safeStep);
        TextView stepChip = text((safeStep + 1) + "  /  3", 10, accent, true);
        stepChip.setGravity(Gravity.CENTER);
        stepChip.setPadding(dp(10), 0, dp(10), 0);
        stepChip.setBackground(round(
                Color.argb(34, Color.red(accent), Color.green(accent), Color.blue(accent)),
                dp(999),
                Color.argb(80, Color.red(accent), Color.green(accent), Color.blue(accent)),
                1
        ));
        heading.addView(stepChip, new LinearLayout.LayoutParams(-2, dp(24)));

        TextView title = habboText(
                safeStep == 0
                        ? t(R.string.tutorial_settings_title)
                        : (safeStep == 1 ? t(R.string.tutorial_search_title) : t(R.string.tutorial_visuals_title)),
                21,
                true
        );
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        title.setMaxLines(2);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2);
        tp.topMargin = dp(5);
        heading.addView(title, tp);

        LinearLayout bodySurface = new LinearLayout(this);
        bodySurface.setOrientation(LinearLayout.VERTICAL);
        bodySurface.setPadding(dp(1), dp(2), dp(1), dp(2));
        bodySurface.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams surfaceLp = new LinearLayout.LayoutParams(-1, -2);
        surfaceLp.topMargin = dp(15);
        card.addView(bodySurface, surfaceLp);

        TextView body = text(
                safeStep == 0
                        ? t(R.string.tutorial_settings_body)
                        : (safeStep == 1 ? t(R.string.tutorial_search_body) : t(R.string.tutorial_visuals_body)),
                14,
                Color.argb(232, 255, 255, 255),
                false
        );
        body.setGravity(Gravity.LEFT);
        body.setLineSpacing(dp(4), 1f);
        bodySurface.addView(body, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams footerLp = new LinearLayout.LayoutParams(-1, dp(44));
        footerLp.topMargin = dp(14);
        card.addView(footer, footerLp);

        LinearLayout dots = new LinearLayout(this);
        dots.setOrientation(LinearLayout.HORIZONTAL);
        dots.setGravity(Gravity.CENTER_VERTICAL);
        footer.addView(dots, new LinearLayout.LayoutParams(0, -1, 1f));
        for (int i = 0; i < 3; i++) {
            View dot = new View(this);
            boolean active = i == safeStep;
            dot.setBackground(active
                    ? grad(dp(999), tutorialAccentSecondaryColor(safeStep), accent)
                    : round(Color.argb(52,255,255,255), dp(999), Color.TRANSPARENT, 0));
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(25), dp(4));
            dotLp.rightMargin = dp(6);
            dots.addView(dot, dotLp);
        }

        final TextView nextButton = habboText(
                (safeStep >= 2 ? t(R.string.tutorial_finish) : t(R.string.tutorial_next)) + "  ›",
                13,
                true
        );
        nextButton.setTextColor(Color.WHITE);
        nextButton.setGravity(Gravity.CENTER);
        nextButton.setSingleLine(true);
        nextButton.setMinWidth(dp(116));
        nextButton.setPadding(dp(17), 0, dp(17), 0);
        nextButton.setBackground(grad(
                dp(999),
                tutorialAccentSecondaryColor(safeStep),
                accent
        ));
        nextButton.setClickable(true);
        nextButton.setFocusable(true);
        footer.addView(nextButton, new LinearLayout.LayoutParams(-2, dp(42)));

        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        cp.setMargins(dp(16), 0, dp(16), dp(80));
        overlay.addView(card, cp);

        final boolean[] leaving = {false};
        final ValueAnimator pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        tutorialPulseAnimator = pulseAnimator;
        pulseAnimator.setDuration(1150L);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.addUpdateListener(animation -> {
            overlayDrawable.setPulse((float) animation.getAnimatedValue());
            overlay.invalidate();
        });

        Runnable advance = () -> {
            if (leaving[0]) return;
            leaving[0] = true;
            try { pulseAnimator.cancel(); } catch (Exception ignored) {}
            if (tutorialPulseAnimator == pulseAnimator) tutorialPulseAnimator = null;
            card.animate()
                    .alpha(0f)
                    .translationY(dp(18))
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(150L)
                    .start();
            overlay.animate()
                    .alpha(0f)
                    .setDuration(180L)
                    .withEndAction(() -> {
                        try { screen.removeView(overlay); } catch (Exception ignored) {}
                        if (tutorialOverlayView == overlay) tutorialOverlayView = null;
                        if (safeStep < 2) uiHandler.postDelayed(() -> showTutorialOverlay(safeStep + 1), 55L);
                    })
                    .start();
        };

        overlay.setOnClickListener(v -> advance.run());
        nextButton.setOnClickListener(v -> advance.run());

        overlay.setAlpha(0f);
        card.setAlpha(0f);
        card.setScaleX(0.94f);
        card.setScaleY(0.94f);
        card.setTranslationY(dp(34));
        screen.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        overlay.bringToFront();
        pulseAnimator.start();
        overlay.animate()
                .alpha(1f)
                .setDuration(190L)
                .start();
        card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(430L)
                .setInterpolator(new android.view.animation.OvershootInterpolator(0.72f))
                .start();
    }

    private void maybeShowProfileFeaturesTutorial() {
        if (screen == null || mainScroll == null) return;
        // The four-part profile tutorial starts only after the initial profile
        // synchronization has fully finished, never on the first fast render.
        if (searchInProgress || profileSectionsInProgress) return;
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean needsAvatarTutorial = sp.getInt(PREF_PROFILE_FEATURES_TUTORIAL_VERSION, 0) < CURRENT_PROFILE_FEATURES_TUTORIAL_VERSION;
        boolean needsFriendTutorial = sp.getInt(PREF_FRIEND_CARD_TUTORIAL_VERSION, 0) < CURRENT_PROFILE_FEATURES_TUTORIAL_VERSION;
        if (!needsAvatarTutorial && (!needsFriendTutorial || profileFriendTutorialTarget == null)) return;
        if (profileFeatureTutorialRunning) return;
        if (tutorialOverlayView != null) {
            uiHandler.postDelayed(this::maybeShowProfileFeaturesTutorial, 450L);
            return;
        }

        final int firstStep = needsAvatarTutorial ? 0 : 3;
        final View firstTarget = firstStep == 3 ? profileFriendTutorialTarget : profileAvatarTutorialTarget;
        if (firstTarget == null) return;
        profileFeatureTutorialRunning = true;
        scrollMainToTutorialTarget(firstTarget, () -> showProfileFeatureTutorial(firstStep));
    }

    private void scrollMainToView(View target, int topMargin) {
        if (target == null || mainScroll == null) return;
        mainScroll.post(() -> {
            if (target.getParent() == null) return;
            Rect rect = new Rect();
            target.getDrawingRect(rect);
            try {
                mainScroll.offsetDescendantRectToMyCoords(target, rect);
                mainScroll.smoothScrollTo(0, Math.max(0, rect.top - Math.max(0, topMargin)));
            } catch (Exception ignored) {}
        });
    }

    private void scrollMainToTutorialTarget(View target, Runnable afterScroll) {
        if (target == null || mainScroll == null) {
            profileFeatureTutorialRunning = false;
            return;
        }
        scrollMainToView(target, dp(82));
        uiHandler.postDelayed(() -> {
            if (target.getParent() == null) {
                profileFeatureTutorialRunning = false;
                return;
            }
            if (afterScroll != null) afterScroll.run();
        }, 420L);
    }

    private RectF tutorialTargetBounds(View target, int paddingDp) {
        return tutorialTargetBounds(screen, target, paddingDp);
    }

    private RectF tutorialTargetBounds(FrameLayout host, View target, int paddingDp) {
        if (target == null || host == null) return null;
        Rect hostVisible = new Rect();
        Rect targetVisible = new Rect();
        if (!host.getGlobalVisibleRect(hostVisible) || !target.getGlobalVisibleRect(targetVisible)) {
            return null;
        }
        if (!targetVisible.intersect(hostVisible)) return null;
        float pad = dp(paddingDp);
        float left = targetVisible.left - hostVisible.left - pad;
        float top = targetVisible.top - hostVisible.top - pad;
        float right = targetVisible.right - hostVisible.left + pad;
        float bottom = targetVisible.bottom - hostVisible.top + pad;
        left = Math.max(dp(6), left);
        top = Math.max(dp(6), top);
        right = Math.min(host.getWidth() - dp(6), right);
        bottom = Math.min(host.getHeight() - dp(6), bottom);
        if (right <= left || bottom <= top) return null;
        return new RectF(left, top, right, bottom);
    }

    private void showProfileFeatureTutorial(final int step) {
        final int safeStep = Math.max(0, Math.min(3, step));
        final View target = safeStep == 3
                ? profileFriendTutorialTarget
                : (safeStep == 2 ? profileFavoriteTutorialTarget : profileAvatarTutorialTarget);
        if (target == null || target.getParent() == null || screen == null) {
            profileFeatureTutorialRunning = false;
            return;
        }
        RectF spotlight = tutorialTargetBounds(target, safeStep >= 2 ? 8 : 10);
        if (spotlight == null) {
            profileFeatureTutorialRunning = false;
            return;
        }

        cancelTutorialPulseAnimation();
        if (tutorialOverlayView != null) detachViewFromParent(tutorialOverlayView);

        final FrameLayout overlay = new FrameLayout(this);
        tutorialOverlayView = overlay;
        if (Build.VERSION.SDK_INT >= 21) overlay.setElevation(dp(80));
        overlay.setClickable(true);
        overlay.setFocusable(true);
        final ProfileTutorialOverlayDrawable overlayDrawable = new ProfileTutorialOverlayDrawable(
                overlay,
                target,
                safeStep >= 2 ? 8 : 10,
                safeStep
        );
        overlay.setBackground(overlayDrawable);

        final LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(16));
        card.setBackground(new TutorialCardDrawable(safeStep));
        card.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(36));

        int accent = tutorialAccentColor(safeStep);
        TextView stepChip = text((safeStep + 1) + "  /  4", 10, accent, true);
        stepChip.setGravity(Gravity.CENTER);
        stepChip.setPadding(dp(10), 0, dp(10), 0);
        stepChip.setBackground(round(
                Color.argb(34, Color.red(accent), Color.green(accent), Color.blue(accent)),
                dp(999),
                Color.argb(80, Color.red(accent), Color.green(accent), Color.blue(accent)),
                1
        ));
        card.addView(stepChip, new LinearLayout.LayoutParams(-2, dp(24)));

        int titleRes;
        int bodyRes;
        if (safeStep == 0) {
            titleRes = R.string.profile_tutorial_rotate_title;
            bodyRes = R.string.profile_tutorial_rotate_body;
        } else if (safeStep == 1) {
            titleRes = R.string.profile_tutorial_looks_title;
            bodyRes = R.string.profile_tutorial_looks_body;
        } else if (safeStep == 2) {
            titleRes = R.string.profile_tutorial_favorite_title;
            bodyRes = R.string.profile_tutorial_favorite_body;
        } else {
            titleRes = R.string.profile_tutorial_friend_title;
            bodyRes = R.string.profile_tutorial_friend_body;
        }

        TextView title = habboText(t(titleRes), 21, true);
        title.setTextColor(Color.WHITE);
        title.setMaxLines(2);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = dp(6);
        card.addView(title, titleLp);

        TextView body = text(t(bodyRes), 14, Color.argb(232, 255, 255, 255), false);
        body.setLineSpacing(dp(4), 1f);
        body.setPadding(dp(14), dp(12), dp(14), dp(12));
        body.setBackground(round(Color.argb(22, 255, 255, 255), dp(17), Color.argb(34, 255, 255, 255), 1));
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(-1, -2);
        bodyLp.topMargin = dp(14);
        card.addView(body, bodyLp);

        TextView nextButton = habboText(
                (safeStep == 3 ? t(R.string.tutorial_finish) : t(R.string.tutorial_next)) + "  ›",
                13,
                true
        );
        nextButton.setTextColor(Color.WHITE);
        nextButton.setGravity(Gravity.CENTER);
        nextButton.setSingleLine(true);
        nextButton.setPadding(dp(18), 0, dp(18), 0);
        nextButton.setBackground(grad(dp(14), tutorialAccentSecondaryColor(safeStep), accent));
        LinearLayout.LayoutParams nextLp = new LinearLayout.LayoutParams(-1, dp(42));
        nextLp.topMargin = dp(14);
        card.addView(nextButton, nextLp);

        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        cardLp.setMargins(dp(16), 0, dp(16), dp(80));
        overlay.addView(card, cardLp);

        final boolean[] leaving = {false};
        final ValueAnimator pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        tutorialPulseAnimator = pulseAnimator;
        pulseAnimator.setDuration(1150L);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.addUpdateListener(animation -> {
            overlayDrawable.setPulse((float) animation.getAnimatedValue());
            overlay.invalidate();
        });

        Runnable advance = () -> {
            if (leaving[0]) return;
            leaving[0] = true;
            try { pulseAnimator.cancel(); } catch (Exception ignored) {}
            if (tutorialPulseAnimator == pulseAnimator) tutorialPulseAnimator = null;
            overlay.animate()
                    .alpha(0f)
                    .setDuration(170L)
                    .withEndAction(() -> {
                        try { screen.removeView(overlay); } catch (Exception ignored) {}
                        if (tutorialOverlayView == overlay) tutorialOverlayView = null;
                        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
                        if (safeStep == 0) {
                            uiHandler.postDelayed(() -> showProfileFeatureTutorial(1), 70L);
                        } else if (safeStep == 1) {
                            uiHandler.postDelayed(() -> showProfileFeatureTutorial(2), 70L);
                        } else if (safeStep == 2) {
                            sp.edit().putInt(PREF_PROFILE_FEATURES_TUTORIAL_VERSION, CURRENT_PROFILE_FEATURES_TUTORIAL_VERSION).apply();
                            boolean needsFriendTutorial = sp.getInt(PREF_FRIEND_CARD_TUTORIAL_VERSION, 0) < CURRENT_PROFILE_FEATURES_TUTORIAL_VERSION;
                            if (needsFriendTutorial && profileFriendTutorialTarget != null && profileFriendTutorialTarget.getParent() != null) {
                                scrollMainToTutorialTarget(profileFriendTutorialTarget, () -> showProfileFeatureTutorial(3));
                            } else {
                                profileFeatureTutorialRunning = false;
                            }
                        } else {
                            sp.edit().putInt(PREF_FRIEND_CARD_TUTORIAL_VERSION, CURRENT_PROFILE_FEATURES_TUTORIAL_VERSION).apply();
                            profileFeatureTutorialRunning = false;
                        }
                    })
                    .start();
        };

        overlay.setOnClickListener(v -> advance.run());
        nextButton.setOnClickListener(v -> advance.run());
        overlay.setAlpha(0f);
        card.setAlpha(0f);
        card.setScaleX(0.95f);
        card.setScaleY(0.95f);
        card.setTranslationY(dp(24));
        screen.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        overlay.bringToFront();
        pulseAnimator.start();
        overlay.animate().alpha(1f).setDuration(190L).start();
        card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(390L)
                .setInterpolator(new android.view.animation.OvershootInterpolator(0.7f))
                .start();
    }

    private void scrollToView(ScrollView scroll, View target, int topMargin, Runnable afterScroll) {
        if (scroll == null || target == null) return;
        scroll.post(() -> {
            if (target.getParent() == null) {
                if (afterScroll != null) afterScroll.run();
                return;
            }
            Rect rect = new Rect();
            target.getDrawingRect(rect);
            try {
                scroll.offsetDescendantRectToMyCoords(target, rect);
                scroll.smoothScrollTo(0, Math.max(0, rect.top - Math.max(0, topMargin)));
                if (afterScroll != null) uiHandler.postDelayed(afterScroll, 380L);
            } catch (Exception ignored) {
                if (afterScroll != null) afterScroll.run();
            }
        });
    }

    private void maybeShowVisualItemTutorial(FrameLayout host, ScrollView visualScroll) {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (sp.getInt(PREF_VISUAL_ITEM_TUTORIAL_VERSION, 0) >= CURRENT_VISUAL_ITEM_TUTORIAL_VERSION) return;
        if (host == null || visualScroll == null || visualItemTutorialTarget == null) return;
        if (visualItemTutorialScheduled || visualItemTutorialRunning) return;
        if (host.getWidth() <= 0 || host.getHeight() <= 0) {
            uiHandler.postDelayed(() -> maybeShowVisualItemTutorial(host, visualScroll), 180L);
            return;
        }
        visualItemTutorialScheduled = true;
        View target = visualItemTutorialTarget;
        scrollToView(visualScroll, target, dp(108), () -> {
            visualItemTutorialScheduled = false;
            if (target.getParent() == null) {
                uiHandler.postDelayed(() -> maybeShowVisualItemTutorial(host, visualScroll), 180L);
                return;
            }
            showVisualItemTutorial(host, target);
        });
    }

    private void showVisualItemTutorial(FrameLayout host, View target) {
        if (host == null || target == null || target.getParent() == null) {
            visualItemTutorialRunning = false;
            return;
        }
        RectF spotlight = tutorialTargetBounds(host, target, 8);
        if (spotlight == null) {
            visualItemTutorialRunning = false;
            return;
        }

        visualItemTutorialRunning = true;
        cancelTutorialPulseAnimation();
        if (visualTutorialOverlayView != null) detachViewFromParent(visualTutorialOverlayView);

        final FrameLayout overlay = new FrameLayout(this);
        visualTutorialOverlayView = overlay;
        if (Build.VERSION.SDK_INT >= 21) overlay.setElevation(dp(80));
        overlay.setClickable(true);
        overlay.setFocusable(true);
        final ProfileTutorialOverlayDrawable overlayDrawable = new ProfileTutorialOverlayDrawable(
                overlay,
                target,
                8,
                0
        );
        overlay.setBackground(overlayDrawable);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(16));
        card.setBackground(new TutorialCardDrawable(0));
        card.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(36));

        TextView title = habboText(t(R.string.visual_item_tutorial_title), 21, true);
        title.setTextColor(Color.WHITE);
        card.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView body = text(t(R.string.visual_item_tutorial_body), 14, Color.argb(232, 255, 255, 255), false);
        body.setLineSpacing(dp(4), 1f);
        body.setPadding(dp(14), dp(12), dp(14), dp(12));
        body.setBackground(round(Color.argb(22, 255, 255, 255), dp(17), Color.argb(34, 255, 255, 255), 1));
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(-1, -2);
        bodyLp.topMargin = dp(14);
        card.addView(body, bodyLp);

        TextView finish = habboText(t(R.string.tutorial_finish) + "  ›", 13, true);
        finish.setTextColor(Color.WHITE);
        finish.setGravity(Gravity.CENTER);
        finish.setBackground(grad(dp(14), tutorialAccentSecondaryColor(0), tutorialAccentColor(0)));
        LinearLayout.LayoutParams finishLp = new LinearLayout.LayoutParams(-1, dp(42));
        finishLp.topMargin = dp(14);
        card.addView(finish, finishLp);

        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        cardLp.setMargins(dp(16), 0, dp(16), dp(80));
        overlay.addView(card, cardLp);

        ValueAnimator pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        tutorialPulseAnimator = pulseAnimator;
        pulseAnimator.setDuration(1150L);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.addUpdateListener(animation -> {
            overlayDrawable.setPulse((float) animation.getAnimatedValue());
            overlay.invalidate();
        });

        final boolean[] leaving = {false};
        Runnable finishTutorial = () -> {
            if (leaving[0]) return;
            leaving[0] = true;
            try { pulseAnimator.cancel(); } catch (Exception ignored) {}
            if (tutorialPulseAnimator == pulseAnimator) tutorialPulseAnimator = null;
            overlay.animate().alpha(0f).setDuration(170L).withEndAction(() -> {
                detachViewFromParent(overlay);
                if (visualTutorialOverlayView == overlay) visualTutorialOverlayView = null;
                visualItemTutorialRunning = false;
                getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                        .putInt(PREF_VISUAL_ITEM_TUTORIAL_VERSION, CURRENT_VISUAL_ITEM_TUTORIAL_VERSION)
                        .apply();
            }).start();
        };

        overlay.setOnClickListener(v -> finishTutorial.run());
        finish.setOnClickListener(v -> finishTutorial.run());
        overlay.setAlpha(0f);
        card.setAlpha(0f);
        card.setTranslationY(dp(24));
        host.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        overlay.bringToFront();
        pulseAnimator.start();
        overlay.animate().alpha(1f).setDuration(190L).start();
        card.animate().alpha(1f).translationY(0f).setDuration(390L)
                .setInterpolator(new android.view.animation.OvershootInterpolator(0.7f)).start();
    }

    private void showStartState() {
        resultWrap.removeAllViews();
        startScreenVisible = activeRenderedProfile == null;
        updateStartNativeAdVisibility();
        if (startScreenVisible) loadStartNativeAdIfNeeded();
    }

    private void setSearchTextProgrammatically(String value) {
        suppressSuggestions = true;
        suggestionRequestId++;
        setSuggestionsVisible(false);
        if (searchInput != null) {
            programmaticSearchTextChange = true;
            searchInput.setText(value == null ? "" : value);
            searchInput.setSelection(searchInput.getText().length());
            programmaticSearchTextChange = false;
        }
    }

    private void search() {
        search(false);
    }

    private void search(boolean searchSlotClaimed) {
        suppressSuggestions = true;
        suggestionRequestId++;
        setSuggestionsVisible(false);
        final String nick = searchInput.getText().toString().trim();
        final String nickKey = normalizeNickKey(nick);
        loadingProfileUniqueIdHint = "";
        loadingProfileFigureHint = "";
        loadingProfileHotelHint = normalizeHotelKey(currentHotelKey);
        if (nickKey.isEmpty()) { hidePullRefreshIndicator(); toast(t(R.string.type_nick_toast)); return; }

        if (searchInProgress && nickKey.equals(activeSearchNick)) {
            hidePullRefreshIndicator();
            toast(t(R.string.same_profile_loading));
            return;
        }

        if (!searchInProgress && activeRenderedProfile != null && nickKey.equals(currentLoadedNick) && normalizeHotelKey(activeRenderedProfile.hotelKey).equals(currentHotelKey)) {
            long now = System.currentTimeMillis();
            long wait = PROFILE_REFRESH_COOLDOWN_MS - (now - lastSameNickRefreshAt);
            if (wait > 0) {
                hidePullRefreshIndicator();
                toast(tr(R.string.wait_refresh, Math.max(1, (int)Math.ceil(wait / 1000.0))));
                return;
            }
        }

        if (!searchSlotClaimed && !claimProfileSearchSlot()) return;

        clearSearchFocus();
        setSuggestionsVisible(false);

        final int token = ++activeSearchToken;
        activeSearchNick = nickKey;
        searchInProgress = true;
        startScreenVisible = false;
        updateStartNativeAdVisibility();
        currentLoadedNick = "";
        currentProfilePrivate = false;
        profileSectionsInProgress = false;
        inlineProgressPct = 0;
        inlineProgressMessage = "";
        visiblePhotosCount = PAGE_CHUNK;
        visibleStylesCount = PAGE_CHUNK;
        photosScrollX = 0;
        stylesScrollX = 0;
        pushCurrentProfileToHistory(nickKey);

        final long loadingStartedAt = SystemClock.elapsedRealtime();
        resultWrap.removeAllViews();
        setLoading(true, t(R.string.searching_profile) + " " + nick + "...");
        maybeShowProfileInterstitial();

        executor.execute(() -> {
            try {
                ProfileResult fresh = loadProfile(nick, false, token);
                if (!isActiveToken(token)) return;

                final ProfileResult r = fresh;
                long releaseAt = loadingStartedAt + PROFILE_MIN_LOADING_MS;
                startProgressiveProfileLoading(r, token, releaseAt);
                awaitMinimumProfileLoading(loadingStartedAt);
                runOnUiThread(() -> finishInitialProfileLoad(r, token, false));
            } catch (ProfileNotFoundException e) {
                awaitMinimumProfileLoading(loadingStartedAt);
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    searchInProgress = false;
                    profileSectionsInProgress = false;
                    activeSearchNick = "";
                    inlineProgressPct = 0;
                    inlineProgressMessage = "";
                    setLoading(false, "");
                    hidePullRefreshIndicator();
                    hidePullRefreshIndicator();
                    showNotFoundState(e.nick, e.suggestions);
                });
            } catch (Exception e) {
                awaitMinimumProfileLoading(loadingStartedAt);
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    searchInProgress = false;
                    profileSectionsInProgress = false;
                    activeSearchNick = "";
                    inlineProgressPct = 0;
                    inlineProgressMessage = "";
                    setLoading(false, "");
                    hidePullRefreshIndicator();
                    hidePullRefreshIndicator();
                    showError(e.getMessage() == null ? t(R.string.error_search_profile) : e.getMessage());
                });
            }
        });
    }

    private String normalizeNickKey(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isActiveToken(int token) {
        return token == activeSearchToken;
    }

    private void awaitMinimumProfileLoading(long loadingStartedAt) {
        long remaining = PROFILE_MIN_LOADING_MS
                - (SystemClock.elapsedRealtime() - loadingStartedAt);
        if (remaining <= 0L) return;
        try {
            Thread.sleep(remaining);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void finishInitialProfileLoad(
            ProfileResult source,
            int token,
            boolean preferUniqueId
    ) {
        if (!isActiveToken(token) || source == null) return;
        searchInProgress = false;
        activeSearchNick = "";
        ProfileResult snapshot;
        synchronized (source) {
            reconcileProfileSources(source);
            enrichPhotoRoomInfo(source);
            snapshot = copyProfileResult(source);
        }

        if (profileSectionsInProgress) {
            if (inlineProgressPct <= 0) inlineProgressPct = 8;
            if (inlineProgressMessage == null || inlineProgressMessage.trim().isEmpty()) {
                inlineProgressMessage = t(R.string.loading_history);
            }
        } else {
            inlineProgressPct = 0;
            inlineProgressMessage = "";
        }
        setLoading(false, "");
        renderProfile(snapshot);
        uiHandler.postDelayed(this::refreshAttachedProfileBannerAds, 160L);
        setStatusMessage("");
        String loadedReference = preferUniqueId && !snapshot.uniqueId.isEmpty()
                ? snapshot.uniqueId
                : snapshot.name;
        currentLoadedNick = normalizeNickKey(loadedReference);
        lastSameNickRefreshAt = System.currentTimeMillis();
        hidePullRefreshIndicator();
        maybeShowProfileFeaturesTutorial();
    }

    private boolean claimProfileSearchSlot() {
        long now = System.currentTimeMillis();
        long wait = PROFILE_SEARCH_COOLDOWN_MS - (now - lastProfileSearchStartedAt);
        if (lastProfileSearchStartedAt > 0L && wait > 0L) {
            hidePullRefreshIndicator();
            toast(tr(R.string.wait_new_search, Math.max(1, (int)Math.ceil(wait / 1000.0))));
            return false;
        }
        lastProfileSearchStartedAt = now;
        return true;
    }

    private boolean isSameLoadedProfileReference(String name, String uniqueId, String hotelKey) {
        if (activeRenderedProfile == null) return false;
        String targetHotel = normalizeHotelKey(hotelKey);
        String loadedHotel = normalizeHotelKey(activeRenderedProfile.hotelKey);
        if (!targetHotel.equals(loadedHotel)) return false;

        String targetId = normalizeNickKey(uniqueId);
        String loadedId = normalizeNickKey(activeRenderedProfile.uniqueId);
        if (!targetId.isEmpty() && !loadedId.isEmpty()) return targetId.equals(loadedId);

        String targetName = normalizeNickKey(name);
        String loadedName = normalizeNickKey(
                activeRenderedProfile.name == null || activeRenderedProfile.name.trim().isEmpty()
                        ? activeRenderedProfile.searchedNick
                        : activeRenderedProfile.name
        );
        return !targetName.isEmpty() && targetName.equals(loadedName);
    }

    private boolean blockRepeatedProfileOpen(String name, String uniqueId, String hotelKey) {
        if (!isSameLoadedProfileReference(name, uniqueId, hotelKey)) return false;
        long wait = PROFILE_REFRESH_COOLDOWN_MS - (System.currentTimeMillis() - lastSameNickRefreshAt);
        if (wait <= 0L) return false;
        hidePullRefreshIndicator();
        toast(tr(R.string.wait_refresh, Math.max(1, (int)Math.ceil(wait / 1000.0))));
        return true;
    }

    private void openSponsorProfile(String name, String uniqueId, String figure, String hotelKey) {
        openProfileReference(name, uniqueId, figure, hotelKey);
    }

    private void updateSelectedHotelHeaderFlag() {
        if (selectedHotelFlag == null) return;
        selectedHotelFlag.setImageDrawable(new HotelFlagDrawable(currentHotelKey));
        selectedHotelFlag.setContentDescription(currentHotelKey.toUpperCase(Locale.ROOT));
        selectedHotelFlag.invalidate();
    }

    private void openProfileReference(String name, String uniqueId, String figure, String hotelKey) {
        String hotel = normalizeHotelKey(hotelKey);
        String targetHotel = hotel.isEmpty() ? currentHotelKey : hotel;
        if (blockRepeatedProfileOpen(name, uniqueId, targetHotel)) return;
        if (!claimProfileSearchSlot()) return;
        if (!hotel.isEmpty()) {
            currentHotelKey = hotel;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_HOTEL, currentHotelKey).apply();
        }
        updateSelectedHotelHeaderFlag();
        String id = uniqueId == null ? "" : uniqueId.trim();
        loadingProfileUniqueIdHint = id;
        loadingProfileFigureHint = figure == null ? "" : figure.trim();
        loadingProfileHotelHint = normalizeHotelKey(currentHotelKey);
        String display = name == null ? "" : name.trim();
        if (display.isEmpty()) display = id;
        setSearchTextProgrammatically(display);
        clearSearchFocus();
        if (!id.isEmpty()) searchByUniqueId(id, display, true);
        else search(true);
    }

    private void searchByUniqueId(final String uniqueId, final String displayNick) {
        searchByUniqueId(uniqueId, displayNick, false);
    }

    private void searchByUniqueId(final String uniqueId, final String displayNick, boolean searchSlotClaimed) {
        suppressSuggestions = true;
        suggestionRequestId++;
        setSuggestionsVisible(false);
        final String id = uniqueId == null ? "" : uniqueId.trim();
        final String idKey = normalizeNickKey(id);
        String previousLoadingIdHint = loadingProfileUniqueIdHint == null ? "" : loadingProfileUniqueIdHint.trim();
        if (!previousLoadingIdHint.equals(id)) loadingProfileFigureHint = "";
        loadingProfileUniqueIdHint = id;
        loadingProfileHotelHint = normalizeHotelKey(currentHotelKey);
        final String shownNick = displayNick == null || displayNick.trim().isEmpty() ? id : displayNick.trim();
        if (idKey.isEmpty()) { search(); return; }

        if (searchInProgress && idKey.equals(activeSearchNick)) {
            hidePullRefreshIndicator();
            toast(t(R.string.same_profile_loading));
            return;
        }

        if (!searchInProgress && blockRepeatedProfileOpen(shownNick, id, currentHotelKey)) {
            return;
        }

        if (!searchSlotClaimed && !claimProfileSearchSlot()) return;

        clearSearchFocus();
        setSuggestionsVisible(false);

        final int token = ++activeSearchToken;
        activeSearchNick = idKey;
        searchInProgress = true;
        startScreenVisible = false;
        updateStartNativeAdVisibility();
        currentLoadedNick = "";
        currentProfilePrivate = false;
        profileSectionsInProgress = false;
        inlineProgressPct = 0;
        inlineProgressMessage = "";
        visiblePhotosCount = PAGE_CHUNK;
        visibleStylesCount = PAGE_CHUNK;
        photosScrollX = 0;
        stylesScrollX = 0;
        pushCurrentProfileToHistory(idKey);

        final long loadingStartedAt = SystemClock.elapsedRealtime();
        resultWrap.removeAllViews();
        setLoading(true, t(R.string.searching_profile) + " " + shownNick + "...");
        maybeShowProfileInterstitial();

        executor.execute(() -> {
            try {
                ProfileResult fresh = loadProfileByUniqueId(id, shownNick, false, token);
                if (!isActiveToken(token)) return;

                final ProfileResult r = fresh;
                long releaseAt = loadingStartedAt + PROFILE_MIN_LOADING_MS;
                startProgressiveProfileLoading(r, token, releaseAt);
                awaitMinimumProfileLoading(loadingStartedAt);
                runOnUiThread(() -> finishInitialProfileLoad(r, token, true));
            } catch (ProfileNotFoundException e) {
                awaitMinimumProfileLoading(loadingStartedAt);
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    searchInProgress = false;
                    profileSectionsInProgress = false;
                    activeSearchNick = "";
                    inlineProgressPct = 0;
                    inlineProgressMessage = "";
                    setLoading(false, "");
                    hidePullRefreshIndicator();
                    showNotFoundState(shownNick, e.suggestions);
                });
            } catch (Exception e) {
                awaitMinimumProfileLoading(loadingStartedAt);
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    searchInProgress = false;
                    profileSectionsInProgress = false;
                    activeSearchNick = "";
                    inlineProgressPct = 0;
                    inlineProgressMessage = "";
                    setLoading(false, "");
                    hidePullRefreshIndicator();
                    showError(e.getMessage() == null ? t(R.string.error_search_profile) : e.getMessage());
                });
            }
        });
    }

    private ProfileResult loadProfileByUniqueId(String uniqueId, String fallbackName, boolean includeSections, int token) throws Exception {
        ProfileResult r = new ProfileResult();
        r.searchedNick = fallbackName == null || fallbackName.trim().isEmpty() ? uniqueId : fallbackName.trim();
        r.uniqueId = uniqueId == null ? "" : uniqueId.trim();
        r.hotelKey = currentHotelKey;

        JSONObject officialUser = r.uniqueId.isEmpty() ? null : validProfileObject(
                tryJson(habboApiUrl("/api/public/users/" + enc(r.uniqueId)))
        );
        JSONObject officialProfile = null;
        r.habboPublic = officialUser;
        JSONObject base = validProfileObject(officialUser);
        JSONObject dexProfile = null;
        if (base == null && !r.uniqueId.isEmpty()) {
            dexProfile = fetchDirectHabbodexProfile(r.uniqueId);
            if (dexProfile != null && !isSameProfileId(r.uniqueId, dexProfile)) dexProfile = null;
            base = validProfileObject(dexProfile);
        }
        if (base == null) throw new ProfileNotFoundException(r.searchedNick, new ArrayList<>());

        if (r.uniqueId.isEmpty()) r.uniqueId = firstText(
                base, "uniqueId", "habboUniqueId", "id", "habboId"
        );
        r.name = firstText(base, "name", "username", "habboName");
        if (r.name.isEmpty()) r.name = r.searchedNick;
        r.figure = firstText(base, "figureString", "figure", "figure_string");
        if (r.figure.isEmpty() && officialUser != null) r.figure = firstText(officialUser, "figureString", "figure", "figure_string");
        if (r.figure.isEmpty()) r.figure = firstText(dexProfile, "figureString", "figure", "figure_string");
        if (!r.figure.isEmpty()) updateLoadingProfileFigureHint(r.figure, token);
        if (r.figure.isEmpty()) r.figure = "hd-180-1";
        r.motto = firstText(base, "motto", "mission");
        if (r.motto.isEmpty() && officialUser != null) r.motto = firstText(officialUser, "motto", "mission");
        if (r.motto.isEmpty()) r.motto = firstText(dexProfile, "motto", "mission");
        r.online = optBoolAny(base, false, "online", "isOnline");
        if (officialUser != null && officialUser.has("online")) r.online = officialUser.optBoolean("online", r.online);
        r.privateProfile = resolveProfilePrivate(officialUser, null, dexProfile, null);
        r.memberSince = firstText(base, "memberSince", "creationTime", "createdAt", "registeredAt", "created_at", "registerDate", "registrationDate");
        if (r.memberSince.isEmpty() && officialUser != null) r.memberSince = firstText(officialUser, "memberSince", "creationTime", "createdAt", "registeredAt", "created_at", "registerDate", "registrationDate");
        if (r.memberSince.isEmpty()) r.memberSince = firstText(dexProfile, "memberSince", "creationTime", "createdAt", "registeredAt", "created_at", "registerDate", "registrationDate");
        r.lastAccess = firstText(base, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
        if (r.lastAccess.isEmpty() && officialUser != null) r.lastAccess = firstText(officialUser, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
        if (r.lastAccess.isEmpty()) r.lastAccess = firstText(dexProfile, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
        r.level = firstText(base, "currentLevel", "level");
        if (r.level.isEmpty()) r.level = firstText(dexProfile, "currentLevel", "level");
        r.starGems = firstText(base, "starGemCount", "starGems");
        if (r.starGems.isEmpty()) r.starGems = firstText(dexProfile, "starGemCount", "starGems");
        // Emblemas são exclusivamente da API oficial do Habbo.
        r.totalBadges = firstText(officialUser, "totalBadges", "badgeCount", "badgesCount", "badgesTotal");
        r.previousNames = extractList(dexProfile, "previousNames");
        r.selectedBadges = extractList(officialUser, "selectedBadges");
        r.dexProfile = dexProfile;
        r.officialProfile = officialProfile;
        r.banned = officialUser == null && resolveBannedFromHistoricalData(dexProfile);
        if (r.banned) {
            r.online = false;
            r.privateProfile = false;
        }
        if (includeSections) completeProfileSections(r, activeSearchToken);
        return r;
    }

    private ProfileResult loadProfile(String nick, boolean includeSections, int token) throws Exception {
        ProfileResult r = new ProfileResult();
        r.searchedNick = nick;
        r.hotelKey = currentHotelKey;

        // Uma busca normal começa exclusivamente pela API oficial. O HabboDex
        // direto só participa desta fase quando o nome não é encontrado.
        JSONObject habboPublic = validProfileObject(
                tryJson(habboApiUrl("/api/public/users?name=" + enc(nick)))
        );
        Future<JSONObject> suggestFuture = habboPublic == null
                ? executor.submit(() -> fetchHabbodexSuggestions(nick))
                : null;
        if (habboPublic != null) {
            updateLoadingProfileFigureHint(
                    firstText(habboPublic, "figureString", "figure", "figure_string"),
                    token
            );
        }

        JSONObject complementByName = null;
        JSONObject suggest = null;
        if (habboPublic == null) {
            try { suggest = suggestFuture.get(30, TimeUnit.SECONDS); }
            catch(Exception ignored) { suggestFuture.cancel(true); }
            complementByName = resolveHabbodexProfileFromSuggestions(suggest, nick);
        }

        JSONObject base = firstObject(habboPublic, complementByName);
        if (base == null) {
            throw new ProfileNotFoundException(
                    nick,
                    filterExactPreviousNickSuggestions(suggest, nick)
            );
        }

        r.habboPublic = habboPublic;
        r.dex = complementByName;
        r.dexProfile = complementByName;
        r.suggest = suggest;
        r.uniqueId = firstText(base, "uniqueId", "habboUniqueId", "id", "habboId");
        r.name = firstText(base, "name", "username", "habboName");
        if (r.name.isEmpty()) r.name = nick;
        r.figure = firstText(base, "figureString", "figure", "figure_string");
        if (!r.figure.isEmpty()) updateLoadingProfileFigureHint(r.figure, token);
        if (r.figure.isEmpty()) r.figure = "hd-180-1";
        r.motto = firstText(base, "motto", "mission");
        r.online = optBoolAny(base, false, "online", "isOnline");
        r.privateProfile = resolveProfilePrivate(
                habboPublic,
                null,
                complementByName,
                null
        );
        r.memberSince = firstText(
                base,
                "memberSince", "creationTime", "createdAt", "registeredAt",
                "created_at", "registerDate", "registrationDate"
        );
        r.lastAccess = firstText(
                base,
                "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit"
        );
        r.level = firstText(base, "currentLevel", "level");
        r.starGems = firstText(base, "starGemCount", "starGems");
        // Emblemas e a contagem de emblemas não usam o HabboDex.
        r.totalBadges = habboPublic == null ? "" : firstText(
                habboPublic,
                "totalBadges", "badgeCount", "badgesCount", "badgesTotal"
        );
        r.previousNames = mergeLists(
                extractList(complementByName, "previousNames"),
                extractPreviousNamesFromSuggest(suggest, r.name)
        );
        r.selectedBadges = habboPublic == null
                ? new ArrayList<>()
                : extractList(habboPublic, "selectedBadges");

        if (r.uniqueId.isEmpty()) {
            throw new ProfileNotFoundException(
                    nick,
                    filterExactPreviousNickSuggestions(suggest, nick)
            );
        }
        r.banned = habboPublic == null && resolveBannedFromHistoricalData(complementByName);
        if (r.banned) {
            r.online = false;
            r.privateProfile = false;
        }
        if (includeSections) completeProfileSections(r, activeSearchToken);
        return r;
    }

    private void completeProfileSections(ProfileResult r, int token) {
        startProgressiveProfileLoading(r, token, 0L);
    }

    private void startProgressiveProfileLoading(
            ProfileResult r,
            int token,
            long firstRenderReleaseAt
    ) {
        if (r == null || r.uniqueId == null || r.uniqueId.trim().isEmpty()
                || !isActiveToken(token)) return;

        activeProfileSource = r;
        final String uniqueId = r.uniqueId.trim();
        final boolean restrictedProfile = !mayLoadOfficialProfileSections(r);
        synchronized (profileProgressLock) {
            if (!isActiveToken(token)) return;
            profileSectionsInProgress = true;
            inlineProgressPct = 8;
            inlineProgressMessage = t(R.string.loading_details);
        }
        updateLoadingSkeletonProgress(token, 8);

        // O perfil principal já foi liberado. Daqui em diante cada seção é
        // independente: uma rota lenta do histórico não segura as demais.
        // +1 tarefa para emblemas paginados do HabboDex. A seção entra em modo
        // paginado antes do perfil oficial responder, impedindo que milhares de
        // emblemas oficiais sejam materializados de uma só vez na interface.
        final int taskCount = restrictedProfile ? 7 : 8;
        final AtomicInteger pendingGroups = new AtomicInteger(taskCount);
        synchronized (r) {
            r.badgesPagedMode = true;
        }
        // O perfil oficial é iniciado uma única vez e compartilhado. A tarefa de
        // amigos espera este resultado antes de liberar a lista, garantindo que
        // nenhum amigo oficial apareça sem a respectiva data do HabboDex.
        final Future<JSONObject> officialProfileFuture = restrictedProfile
                ? null
                : executor.submit(() -> tryJson(
                        habboApiUrl("/api/public/users/" + enc(uniqueId) + "/profile")
                ));

        // 1) Nomes anteriores são críticos. Não aceita uma resposta vazia transitória
        // do cache como confirmação definitiva: tenta rota dedicada, perfil completo
        // e busca por nome antes de concluir que não existe histórico.
        profileSectionsExecutor.execute(() -> {
            try {
                ProfileSectionPayload names = fetchCriticalPreviousNames(
                        uniqueId,
                        r.name
                );
                if (!isActiveToken(token)) return;
                if (names.success || !names.items.isEmpty()) {
                    synchronized (r) {
                        if (!names.items.isEmpty()) {
                            putComplementSectionLocked(r, "previousNames", names.items);
                        }
                        reconcileProfileSources(r);
                        enrichPhotoRoomInfo(r);
                    }
                    setProfileSectionsProgress(token, 28, t(R.string.loading_history));
                    publishProgressiveProfile(r, token, firstRenderReleaseAt);
                }
            } finally {
                finishProfileSectionsGroup(r, token, firstRenderReleaseAt, pendingGroups);
            }
        });

        // 2) Perfil completo do HabboDex serve apenas como enriquecimento/fallback.
        profileSectionsExecutor.execute(() -> {
            try {
                // Dá vantagem às duas seções críticas (nomes e amigos).
                sleepCriticalRetry(1);
                JSONObject historicalProfile = fetchDirectHabbodexProfile(uniqueId);
                if (!isActiveToken(token) || historicalProfile == null) return;
                synchronized (r) {
                    applyComplementProfileData(
                            r,
                            mergeComplementPayloads(historicalProfile, r.dexProfile)
                    );
                    reconcileProfileSources(r);
                    enrichPhotoRoomInfo(r);
                }
                setProfileSectionsProgress(token, 36, t(R.string.loading_history));
                publishProgressiveProfile(r, token, firstRenderReleaseAt);
            } finally {
                finishProfileSectionsGroup(r, token, firstRenderReleaseAt, pendingGroups);
            }
        });

        // 3) Missões e visuais: somente a primeira página no carregamento inicial.
        profileSectionsExecutor.execute(() -> {
            try {
                sleepCriticalRetry(1);
                JSONObject secondary = fetchDirectHabbodexPriorityBatch(
                        uniqueId,
                        "previous-mottos,previous-styles"
                );
                if (!isActiveToken(token)) return;
                final JSONObject fallback;
                synchronized (r) { fallback = copyJsonObject(r.dexProfile); }
                ProfileSectionPayload mottos = historySectionWithDirectFallback(
                        secondary, fallback, uniqueId,
                        "previous-mottos", "previousMottos", 1
                );
                ProfileSectionPayload styles = historySectionWithDirectFallback(
                        secondary, fallback, uniqueId,
                        "previous-styles", "previousStyles", 1
                );
                synchronized (r) {
                    if (mottos.success || !mottos.items.isEmpty()) {
                        putComplementSectionLocked(r, "previousMottos", mottos.items);
                    }
                    if (styles.success || !styles.items.isEmpty()) {
                        putComplementSectionLocked(r, "previousStyles", styles.items);
                        int remoteTotal = extractBatchSectionTotal(secondary, "previousStyles");
                        int fetchedCount = r.allStylesSource == null ? 0 : r.allStylesSource.size();
                        int visibleCount = r.previousStyles == null ? 0 : r.previousStyles.size();
                        r.stylesRemotePaged = remoteTotal > fetchedCount
                                || (remoteTotal <= 0 && styles.items.size() >= 100);
                        r.stylesRemoteNextPage = r.stylesRemotePaged ? 2 : 0;
                        if (remoteTotal > 0) r.stylesTotal = Math.max(remoteTotal, fetchedCount);
                        else r.stylesTotal = Math.max(r.stylesTotal, fetchedCount);
                        r.stylesHasMore = visibleCount < fetchedCount || r.stylesRemotePaged;
                        r.stylesNextPage = r.stylesHasMore ? 2 : 0;
                    }
                    reconcileProfileSources(r);
                    enrichPhotoRoomInfo(r);
                }
                setProfileSectionsProgress(token, 48, t(R.string.loading_styles_friends));
                publishProgressiveProfile(r, token, firstRenderReleaseAt);
            } finally {
                finishProfileSectionsGroup(r, token, firstRenderReleaseAt, pendingGroups);
            }
        });

        // 4) Amigos atuais são críticos, mas agora usam paginação progressiva.
        // A primeira página (100 itens) é liberada assim que TODOS esses itens tiverem data.
        // As páginas seguintes são buscadas somente quando o usuário alcança o fim do lote carregado.
        profileSectionsExecutor.execute(() -> {
            try {
                PageResult firstFriends = fetchCriticalFriendsPage(
                        uniqueId,
                        1,
                        100,
                        true
                );
                if (!isActiveToken(token)) return;
                JSONObject officialForFriends = restrictedProfile
                        ? null
                        : awaitFutureValue(officialProfileFuture);

                if (firstFriends != null
                        && firstFriends.success
                        && firstFriends.items != null
                        && !firstFriends.items.isEmpty()
                        && allFriendsHaveAddedDates(firstFriends.items)) {
                    synchronized (r) {
                        if (!restrictedProfile) {
                            r.officialProfileAttempted = true;
                            if (officialForFriends != null) {
                                applyOfficialProfileData(r, officialForFriends);
                            }
                        }

                        ArrayList<JSONObject> datedPage = new ArrayList<>(firstFriends.items);
                        ArrayList<JSONObject> officialFriends = officialForFriends == null
                                ? new ArrayList<>()
                                : extractList(officialForFriends, "friends");
                        // Mantém apenas os amigos já validados pelo HabboDex; a API oficial
                        // apenas enriquece os mesmos registros e nunca adiciona amigos sem data.
                        datedPage = mergeListsEnrichingPrimary(datedPage, officialFriends, false);

                        r.friendsPagedMode = true;
                        r.friends = datedPage;
                        r.friendsTotal = Math.max(firstFriends.total, officialFriends.size());
                        r.friendsNextPage = firstFriends.nextPage;
                        r.friendsHasMore = firstFriends.hasMore
                                || (r.friendsTotal > 0 && r.friends.size() < r.friendsTotal);
                        if (r.friendsHasMore && r.friendsNextPage <= 1) {
                            r.friendsNextPage = 2;
                        }
                        r.friendsDatesReady = true;
                        reconcileProfileSources(r);
                        enrichPhotoRoomInfo(r);

                        // Se removidos abriu automaticamente enquanto os amigos carregavam,
                        // volta para a aba principal. Escolha manual continua sendo respeitada.
                        if (!r.friendsTabSelectionTouched) {
                            r.friendsTabShowingRemoved = false;
                            r.friendsTabPage = 1;
                        }
                    }
                    setProfileSectionsProgress(token, 76, t(R.string.loading_history));
                    publishProgressiveProfile(r, token, firstRenderReleaseAt);
                }
            } finally {
                finishProfileSectionsGroup(r, token, firstRenderReleaseAt, pendingGroups);
            }
        });

        // 5) Amigos removidos não bloqueiam os atuais e começam com uma página.
        profileSectionsExecutor.execute(() -> {
            try {
                sleepCriticalRetry(1);
                PageResult removed = fetchPage(
                        uniqueId,
                        "previous-friends",
                        "previousFriends",
                        1,
                        100
                );
                if (!isActiveToken(token)) return;
                if (removed.success) {
                    synchronized (r) {
                        putComplementSectionLocked(r, "previousFriends", removed.items);
                        r.removedFriendsTotal = Math.max(removed.total, removed.items.size());
                        r.removedFriendsNextPage = removed.nextPage;
                        r.removedFriendsHasMore = removed.hasMore
                                || r.removedFriendsTotal > removed.items.size();
                        if (r.removedFriendsHasMore && r.removedFriendsNextPage <= 1) {
                            r.removedFriendsNextPage = 2;
                        }
                        reconcileProfileSources(r);
                        enrichPhotoRoomInfo(r);
                    }
                    setProfileSectionsProgress(token, 84, t(R.string.loading_history));
                    publishProgressiveProfile(r, token, firstRenderReleaseAt);
                }
            } finally {
                finishProfileSectionsGroup(r, token, firstRenderReleaseAt, pendingGroups);
            }
        });

        // 6) Emblemas: primeira página de 100 pelo HabboDex. O restante é
        // buscado automaticamente quando o usuário chega ao limite carregado,
        // exatamente como amigos e amigos removidos.
        profileSectionsExecutor.execute(() -> {
            try {
                sleepCriticalRetry(2);
                PageResult firstBadges = fetchCriticalBadgesPage(uniqueId, 1, 100, true);
                if (!isActiveToken(token)) return;
                if (firstBadges != null && firstBadges.success) {
                    synchronized (r) {
                        applyBadgesPage(r, firstBadges, true);
                        reconcileProfileSources(r);
                        enrichSelectedBadgesWithOwnership(r);
                    }
                    setProfileSectionsProgress(token, 88, t(R.string.loading_history));
                    publishProgressiveProfile(r, token, firstRenderReleaseAt);
                }
            } finally {
                finishProfileSectionsGroup(r, token, firstRenderReleaseAt, pendingGroups);
            }
        });

        if (!restrictedProfile) {
            // 7) Perfil completo oficial.
            profileSectionsExecutor.execute(() -> {
                try {
                    JSONObject official = awaitFutureValue(officialProfileFuture);
                    if (!isActiveToken(token)) return;
                    synchronized (r) {
                        r.officialProfileAttempted = true;
                        if (official != null) applyOfficialProfileData(r, official);
                        reconcileProfileSources(r);
                        // A lista completa de emblemas já é oficial; enriquece os
                        // selecionados uma única vez, evitando varrer milhares de
                        // emblemas a cada atualização progressiva do perfil.
                        enrichSelectedBadgesWithOwnership(r);
                        enrichPhotoRoomInfo(r);
                    }
                    setProfileSectionsProgress(token, 58, t(R.string.loading_history));
                    publishProgressiveProfile(r, token, firstRenderReleaseAt);
                } finally {
                    finishProfileSectionsGroup(r, token, firstRenderReleaseAt, pendingGroups);
                }
            });

            // 8) Fotos oficiais são totalmente independentes do histórico.
            profileSectionsExecutor.execute(() -> {
                try {
                    ProfileSectionPayload photos;
                    try {
                        photos = ProfileSectionPayload.list(
                                "photos",
                                fetchOfficialPhotos(uniqueId),
                                true
                        );
                    } catch(Exception ignored) {
                        photos = ProfileSectionPayload.list(
                                "photos",
                                new ArrayList<>(),
                                false
                        );
                    }
                    if (!isActiveToken(token)) return;
                    synchronized (r) {
                        applyOfficialPhotosData(r, photos.items, photos.success);
                        reconcileProfileSources(r);
                        enrichPhotoRoomInfo(r);
                    }
                    setProfileSectionsProgress(token, 68, t(R.string.loading_styles_friends));
                    publishProgressiveProfile(r, token, firstRenderReleaseAt);
                } finally {
                    finishProfileSectionsGroup(r, token, firstRenderReleaseAt, pendingGroups);
                }
            });
        } else {
            synchronized (r) {
                r.officialProfileAttempted = true;
                r.officialPhotosAttempted = true;
                r.officialPhotosSucceeded = false;
            }

            // 7) Dados exclusivos de perfil privado: somente a primeira página.
            profileSectionsExecutor.execute(() -> {
                try {
                    JSONObject privateDetails = fetchDirectHabbodexPriorityBatch(
                            uniqueId,
                            true,
                            "rooms,groups,photos"
                    );
                    if (!isActiveToken(token) || privateDetails == null) return;
                    synchronized (r) {
                        ArrayList<JSONObject> rooms = extractDirectHistoryItems(privateDetails, "rooms");
                        ArrayList<JSONObject> groups = extractDirectHistoryItems(privateDetails, "groups");
                        ArrayList<JSONObject> photos = extractDirectHistoryItems(privateDetails, "photos");
                        putComplementSectionLocked(r, "rooms", rooms);
                        putComplementSectionLocked(r, "groups", groups);
                        putComplementSectionLocked(r, "photos", photos);
                        reconcileProfileSources(r);
                        enrichPhotoRoomInfo(r);
                    }
                    setProfileSectionsProgress(token, 94, t(R.string.loading_details));
                    publishProgressiveProfile(r, token, firstRenderReleaseAt);
                } finally {
                    finishProfileSectionsGroup(r, token, firstRenderReleaseAt, pendingGroups);
                }
            });
        }

        // Emblemas são paginados pelo HabboDex; a API oficial apenas enriquece
        // metadados dos itens já carregados e mantém a contagem total.
    }

    private boolean mayLoadOfficialProfileSections(ProfileResult profile) {
        if (profile == null || profile.privateProfile || profile.banned) return false;
        Boolean officialVisibility = explicitProfileVisibility(
                profile.habboPublic,
                profile.officialProfile
        );
        if (officialVisibility != null) return officialVisibility;
        Boolean complementVisibility = explicitProfileVisibility(
                profile.dexProfile,
                profile.dex
        );
        return Boolean.TRUE.equals(complementVisibility);
    }

    private void setProfileSectionsProgress(int token, int pct, String message) {
        int updated;
        synchronized (profileProgressLock) {
            if (!isActiveToken(token) || !profileSectionsInProgress) return;
            int bounded = Math.max(0, Math.min(99, pct));
            if (bounded < inlineProgressPct) return;
            inlineProgressPct = bounded;
            inlineProgressMessage = message == null ? "" : message;
            updated = inlineProgressPct;
        }
        updateLoadingSkeletonProgress(token, updated);
    }

    private void finishProfileSectionsGroup(
            ProfileResult source,
            int token,
            long firstRenderReleaseAt,
            AtomicInteger pendingGroups
    ) {
        if (pendingGroups == null || pendingGroups.decrementAndGet() > 0
                || !isActiveToken(token)) return;

        synchronized (profileProgressLock) {
            if (!isActiveToken(token)) return;
            if (searchInProgress) {
                // As fontes terminaram antes do primeiro desenho. O perfil inicial
                // já receberá o estado final, sem um render intermediário a 100%.
                profileSectionsInProgress = false;
                inlineProgressPct = 0;
                inlineProgressMessage = "";
                updateLoadingSkeletonProgress(token, 100);
                return;
            }
            // Não redesenha o perfil inteiro duas vezes para exibir 100% e logo
            // depois ocultar o indicador. Fecha o progresso e publica uma única vez.
            profileSectionsInProgress = false;
            inlineProgressPct = 0;
            inlineProgressMessage = "";
        }
        publishProgressiveProfile(source, token, firstRenderReleaseAt);
        runOnUiThread(() -> uiHandler.postDelayed(() -> {
            if (isActiveToken(token) && !searchInProgress && !profileSectionsInProgress) {
                maybeShowProfileFeaturesTutorial();
            }
        }, 520L));
    }

    private <T> T awaitFutureValue(Future<T> future) {
        if (future == null) return null;
        try {
            return future.get();
        } catch(Exception ignored) {
            future.cancel(true);
            return null;
        }
    }

    private JSONObject fetchDirectHabbodexPriorityBatch(
            String uniqueId,
            String sections
    ) {
        return fetchDirectHabbodexPriorityBatch(uniqueId, false, sections);
    }

    private JSONObject fetchDirectHabbodexPriorityBatch(
            String uniqueId,
            boolean includePrivate,
            String sections
    ) {
        String cleanId = uniqueId == null ? "" : uniqueId.trim();
        if (cleanId.isEmpty()) return null;
        return fetchHabbodexBatchDirect(cleanId, includePrivate, sections, 1);
    }

    private PageResult fetchHabbodexPages(
            String uniqueId,
            String endpoint,
            String primaryKey,
            int maxPages
    ) {
        PageResult combined = new PageResult();
        combined.page = 1;
        combined.nextPage = 0;
        combined.total = 0;
        combined.hasMore = false;
        combined.success = false;

        int page = 1;
        int pageLimit = Math.max(1, Math.min(25, maxPages));
        HashSet<String> seen = new HashSet<>();

        for (int request = 0; request < pageLimit; request++) {
            PageResult part = fetchPage(uniqueId, endpoint, primaryKey, page, 100);
            if (part == null || !part.success) {
                combined.hasMore = combined.success;
                break;
            }
            combined.success = true;
            combined.total = Math.max(combined.total, part.total);

            for (JSONObject item : part.items) {
                if (item == null) continue;
                String fingerprint = item.toString();
                if (seen.add(fingerprint)) combined.items.add(item);
            }

            if (!part.hasMore || part.nextPage <= page) {
                combined.nextPage = 0;
                combined.hasMore = false;
                break;
            }

            combined.nextPage = part.nextPage;
            combined.hasMore = true;
            page = part.nextPage;
        }

        if (combined.total > 0 && combined.items.size() >= combined.total) {
            combined.hasMore = false;
            combined.nextPage = 0;
        }
        return combined;
    }

    private JSONObject fetchHabbodexBatchDirect(
            String uniqueId,
            boolean includePrivate,
            String sections,
            int maxPages
    ) {
        String cleanId = uniqueId == null ? "" : uniqueId.trim();
        if (cleanId.isEmpty()) return null;

        LinkedHashMap<String, String[]> definitions = new LinkedHashMap<>();
        definitions.put("profile", new String[]{"profile", ""});
        definitions.put("previous-names", new String[]{"previousNames", "previous-names"});
        definitions.put("friends", new String[]{"friends", "friends"});
        definitions.put("previous-friends", new String[]{"previousFriends", "previous-friends"});
        definitions.put("previous-mottos", new String[]{"previousMottos", "previous-mottos"});
        definitions.put("previous-styles", new String[]{"previousStyles", "previous-styles"});
        if (includePrivate) {
            definitions.put("rooms", new String[]{"rooms", "rooms"});
            definitions.put("groups", new String[]{"groups", "groups"});
            definitions.put("photos", new String[]{"photos", "photos"});
        }

        LinkedHashSet<String> requested = new LinkedHashSet<>();
        String cleanSections = sections == null ? "" : sections.trim();
        if (cleanSections.isEmpty()) {
            requested.addAll(definitions.keySet());
        } else {
            for (String raw : cleanSections.split(",")) {
                String section = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
                if (definitions.containsKey(section)) requested.add(section);
            }
        }
        // Emblemas nunca entram no complemento: permanecem exclusivos da API Habbo.
        requested.remove("badges");
        if (requested.isEmpty()) return null;

        LinkedHashMap<String, Future<Object>> tasks = new LinkedHashMap<>();
        for (String section : requested) {
            String[] def = definitions.get(section);
            if (def == null) continue;
            if ("profile".equals(section)) {
                tasks.put(section, executor.submit(() -> fetchDirectHabbodexProfile(cleanId)));
            } else {
                String key = def[0];
                String endpoint = def[1];
                tasks.put(section, executor.submit(
                        () -> fetchHabbodexPages(cleanId, endpoint, key, maxPages)
                ));
            }
        }

        JSONObject out = new JSONObject();
        JSONObject totals = new JSONObject();
        JSONObject errors = new JSONObject();
        boolean partial = false;
        int successes = 0;

        for (Map.Entry<String, Future<Object>> entry : tasks.entrySet()) {
            String section = entry.getKey();
            String[] def = definitions.get(section);
            String key = def == null ? section : def[0];
            Object value = null;
            try {
                value = entry.getValue().get();
            } catch(Exception error) {
                entry.getValue().cancel(true);
            }

            try {
                if ("profile".equals(section)) {
                    JSONObject profile = value instanceof JSONObject ? (JSONObject)value : null;
                    if (profile != null) {
                        out.put("profile", profile);
                        successes++;
                    } else {
                        errors.put("profile", "request_failed");
                        partial = true;
                    }
                    continue;
                }

                PageResult page = value instanceof PageResult ? (PageResult)value : null;
                if (page == null || !page.success) {
                    errors.put(key, "request_failed");
                    partial = true;
                    continue;
                }

                out.put(key, jsonArrayFromObjects(page.items));
                totals.put(key, page.total > 0 ? page.total : page.items.size());
                successes++;
                if (page.hasMore) partial = true;
            } catch(Exception ignored) {
                try { errors.put(key, "parse_failed"); } catch(Exception ignoredAgain) {}
                partial = true;
            }
        }

        if (successes == 0) return null;
        try {
            out.put("totals", totals);
            out.put("errors", errors);
            out.put("partial", partial || errors.length() > 0);
            out.put("_toxicHabbodexDirect", true);
        } catch(Exception ignored) {}
        return out;
    }

    private ProfileSectionPayload historySectionFromBatch(
            JSONObject batch,
            String key
    ) {
        if (batch == null) {
            return ProfileSectionPayload.list(key, new ArrayList<>(), false);
        }
        ArrayList<JSONObject> items = extractList(batch, key);
        JSONObject errors = batch.optJSONObject("errors");
        // "partial" descreve o lote inteiro. Uma falha em amigos não deve
        // invalidar nomes, visuais ou missões que já vieram corretamente.
        boolean success = hasNamedListDeep(batch, key)
                && (errors == null || !errors.has(key));
        JSONObject totals = batch.optJSONObject("totals");
        if (totals != null && totals.has(key)
                && totals.optInt(key, items.size()) > items.size()) {
            success = false;
        }
        return ProfileSectionPayload.list(key, items, success);
    }

    private ProfileSectionPayload historySectionWithDirectFallback(
            JSONObject batch,
            JSONObject profileFallback,
            String uniqueId,
            String endpoint,
            String key,
            int maxPages
    ) {
        ProfileSectionPayload fromBatch = historySectionFromBatch(batch, key);
        ProfileSectionPayload fromProfile = ProfileSectionPayload.list(
                key,
                extractList(profileFallback, key),
                hasNamedListDeep(profileFallback, key)
        );
        ArrayList<JSONObject> available = mergeListsEnrichingPrimary(
                fromBatch.items,
                fromProfile.items,
                true
        );
        boolean confirmEmptyPreviousNames = "previousNames".equals(key)
                && available.isEmpty();
        if (!confirmEmptyPreviousNames && (
                fromBatch.success
                // Um lote pode terminar parcial por atingir o limite de páginas.
                // Os itens já recebidos continuam válidos; começar novamente na
                // página 1 só duplica chamadas e atrasa a aplicação das datas.
                || !fromBatch.items.isEmpty()
                || (fromProfile.success && !available.isEmpty())
        )) {
            return ProfileSectionPayload.list(key, available, true);
        }

        ProfileSectionPayload fromList = fetchDirectHabbodexListSection(
                uniqueId,
                endpoint,
                key,
                maxPages
        );
        ArrayList<JSONObject> combined = mergeListsEnrichingPrimary(
                available,
                fromList.items,
                true
        );
        return ProfileSectionPayload.list(
                key,
                combined,
                fromList.success || fromProfile.success
        );
    }

    private void sleepCriticalRetry(int attempt) {
        try {
            Thread.sleep(attempt <= 0 ? 180L : Math.min(700L, 220L + (attempt * 180L)));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void invalidateFiveMinuteJsonCache(String url) {
        if (url == null || url.trim().isEmpty()) return;
        jsonResponseCache.remove(url);
        try {
            File file = fiveMinuteJsonCacheFile(url);
            if (file != null && file.isFile()) file.delete();
        } catch(Exception ignored) {}
    }

    private JSONObject tryJsonFresh(String url) {
        invalidateFiveMinuteJsonCache(url);
        return tryJson(url);
    }

    private JSONObject fetchDirectHabbodexProfileFresh(String uniqueId) {
        String cleanId = uniqueId == null ? "" : uniqueId.trim();
        if (cleanId.isEmpty()) return null;
        JSONObject profile = extractHabbodexProfilePayload(
                unwrap(tryJsonFresh(habbodexProfileUrl(cleanId)))
        );
        if (profile == null || !isSameProfileId(cleanId, profile)) return null;
        profile.remove("badges");
        // selectedBadges é pequeno e útil para trazer Obtido em dos emblemas
        // selecionados sem baixar a coleção completa.
        profile.remove("totalBadges");
        profile.remove("badgeCount");
        profile.remove("badgesCount");
        profile.remove("badgesTotal");
        return profile;
    }

    private ProfileSectionPayload fetchCriticalPreviousNames(
            String uniqueId,
            String currentName
    ) {
        ArrayList<JSONObject> combined = new ArrayList<>();
        String cleanId = uniqueId == null ? "" : uniqueId.trim();
        String cleanName = currentName == null ? "" : currentName.trim();
        if (cleanId.isEmpty()) {
            return ProfileSectionPayload.list("previousNames", combined, false);
        }

        boolean receivedAnyValidResponse = false;
        String listUrl = habbodexListUrl(cleanId, "previous-names", 1, 100);

        // Resposta vazia pode ser transitória. A primeira tentativa pode usar o cache;
        // as seguintes são obrigatoriamente frescas para não repetir um vazio cacheado.
        for (int attempt = 0; attempt < 3 && combined.isEmpty(); attempt++) {
            JSONObject response = unwrap(attempt == 0 ? tryJson(listUrl) : tryJsonFresh(listUrl));
            if (response != null) {
                receivedAnyValidResponse = true;
                ArrayList<JSONObject> items = extractDirectHistoryItems(response, "previousNames");
                combined = mergeListsEnrichingPrimary(combined, items, true);
                if (!combined.isEmpty()) break;
            }
            if (attempt < 2) sleepCriticalRetry(attempt);
        }

        // O perfil completo costuma conter previousNames mesmo quando a rota dedicada
        // responde [] temporariamente.
        if (combined.isEmpty()) {
            JSONObject profile = fetchDirectHabbodexProfileFresh(cleanId);
            if (profile != null) {
                receivedAnyValidResponse = true;
                combined = mergeListsEnrichingPrimary(
                        combined,
                        extractDirectHistoryItems(profile, "previousNames"),
                        true
                );
            }
        }

        // Último caminho independente: busca por nick com includePreviousNames=true.
        if (combined.isEmpty() && !cleanName.isEmpty()) {
            String suggestUrl = habbodexSuggestUrl(cleanName, currentHotelKey);
            JSONObject suggest = unwrap(tryJsonFresh(suggestUrl));
            if (suggest != null) {
                receivedAnyValidResponse = true;
                combined = mergeListsEnrichingPrimary(
                        combined,
                        extractPreviousNamesFromSuggest(suggest, cleanName),
                        true
                );
            }
        }

        return ProfileSectionPayload.list(
                "previousNames",
                combined,
                receivedAnyValidResponse
        );
    }

    private boolean friendHasAddedDate(JSONObject friend) {
        if (friend == null) return false;
        return !firstText(
                friend,
                "creationTime", "friendSince", "addedAt", "createdAt", "date",
                "since", "friendshipSince", "friend_since", "detectedAt", "detected_at"
        ).isEmpty();
    }

    private boolean allFriendsHaveAddedDates(ArrayList<JSONObject> friends) {
        if (friends == null || friends.isEmpty()) return false;
        for (JSONObject friend : friends) {
            if (!friendHasAddedDate(friend)) return false;
        }
        return true;
    }

    private PageResult fetchCriticalFriendsPage(
            String uniqueId,
            int page,
            int limit,
            boolean requireItems
    ) {
        PageResult best = null;
        String url = habbodexListUrl(uniqueId, "friends", page, limit);
        for (int attempt = 0; attempt < 3; attempt++) {
            if (attempt > 0) invalidateFiveMinuteJsonCache(url);
            PageResult candidate = fetchPage(uniqueId, "friends", "friends", page, limit);
            if (candidate != null && candidate.success) {
                best = candidate;
                boolean hasItems = candidate.items != null && !candidate.items.isEmpty();
                boolean datesReady = hasItems && allFriendsHaveAddedDates(candidate.items);
                if ((!requireItems && !hasItems) || datesReady) return candidate;
            }
            if (attempt < 2) sleepCriticalRetry(attempt);
        }
        return best == null ? new PageResult() : best;
    }

    private ProfileSectionPayload fetchDirectHabbodexListSection(
            String uniqueId,
            String endpoint,
            String key,
            int maxPages
    ) {
        ArrayList<JSONObject> combined = new ArrayList<>();
        String cleanId = uniqueId == null ? "" : uniqueId.trim();
        if (cleanId.isEmpty()) {
            return ProfileSectionPayload.list(key, combined, false);
        }

        final int limit = 100;
        final int pageLimit = Math.max(1, maxPages);
        int page = 1;
        int knownTotal = 0;
        boolean received = false;
        boolean complete = false;

        for (int request = 0; request < pageLimit; request++) {
            JSONObject response = unwrap(tryJson(
                    habbodexListUrl(cleanId, endpoint, page, limit)
            ));
            if (response == null
                    || (response.has("ok") && !response.optBoolean("ok", true))) {
                return ProfileSectionPayload.list(key, combined, false);
            }
            received = true;

            ArrayList<JSONObject> items = extractDirectHistoryItems(response, key);
            if (request == 0 && page == 1
                    && "previousNames".equals(key)
                    && items.isEmpty()) {
                JSONObject confirmed = fetchDirectHabbodexProfile(cleanId);
                ArrayList<JSONObject> confirmedItems = extractDirectHistoryItems(
                        confirmed,
                        key
                );
                if (confirmed != null && !confirmedItems.isEmpty()) {
                    items = confirmedItems;
                }
            }
            combined = mergeListsEnrichingPrimary(combined, items, true);
            knownTotal = Math.max(knownTotal, extractTotalCount(response));

            JSONObject next = response.optJSONObject("next");
            int nextPage = next == null ? 0 : next.optInt("page", 0);
            JSONObject pagination = response.optJSONObject("pagination");
            if (nextPage <= 0 && pagination != null) {
                nextPage = pagination.optInt("nextPage", 0);
            }
            int totalPages = response.optInt(
                    "totalPages",
                    response.optInt("pages", 0)
            );
            if (pagination != null) {
                totalPages = Math.max(
                        totalPages,
                        pagination.optInt(
                                "totalPages",
                                pagination.optInt("pages", 0)
                        )
                );
            }
            if (nextPage <= page && totalPages > page) nextPage = page + 1;
            if (nextPage <= page && knownTotal > combined.size()) nextPage = page + 1;
            if (nextPage <= page && items.size() >= limit) nextPage = page + 1;

            if (nextPage <= page) {
                complete = true;
                break;
            }
            if (nextPage > pageLimit) {
                complete = knownTotal <= 0 || combined.size() >= knownTotal;
                break;
            }
            page = nextPage;
        }

        if (knownTotal > 0 && combined.size() < knownTotal) complete = false;
        return ProfileSectionPayload.list(
                key,
                combined,
                received && complete
        );
    }

    private ArrayList<JSONObject> extractDirectHistoryItems(
            JSONObject response,
            String key
    ) {
        ArrayList<JSONObject> items = extractList(response, key);
        if (!items.isEmpty()) return items;
        if ("previousNames".equals(key)) return extractList(response, "names");
        if ("previousMottos".equals(key)) return extractList(response, "mottos");
        if ("previousStyles".equals(key)) return extractList(response, "styles");
        if ("previousFriends".equals(key)) return extractList(response, "friends");
        return items;
    }

    private void putComplementSectionLocked(
            ProfileResult r,
            String key,
            ArrayList<JSONObject> items
    ) {
        if (r == null || key == null || key.isEmpty()) return;
        JSONObject complement = copyJsonObject(r.dexProfile);
        if (complement == null) complement = new JSONObject();
        try {
            complement.put(key, jsonArrayFromObjects(items));
        } catch(Exception ignored) {}
        applyComplementProfileData(r, complement);
    }

    private JSONObject copyJsonObject(JSONObject source) {
        if (source == null) return null;
        try {
            return new JSONObject(source.toString());
        } catch(Exception ignored) {
            return null;
        }
    }

    private void publishProgressiveProfile(
            ProfileResult source,
            int token,
            long firstRenderReleaseAt
    ) {
        if (source == null || !isActiveToken(token) || searchInProgress) return;
        if (firstRenderReleaseAt > 0L
                && SystemClock.elapsedRealtime() < firstRenderReleaseAt) return;

        final ProfileResult snapshot;
        synchronized (source) {
            snapshot = copyProfileResult(source);
        }

        long delay;
        synchronized (progressiveRenderLock) {
            pendingProgressiveSnapshot = snapshot;
            pendingProgressiveToken = token;
            if (progressiveRenderScheduled) return;
            long elapsed = SystemClock.elapsedRealtime() - lastProgressiveRenderAtMs;
            delay = Math.max(0L, PROGRESSIVE_RENDER_MIN_INTERVAL_MS - elapsed);
            progressiveRenderScheduled = true;
        }

        uiHandler.postDelayed(() -> {
            final ProfileResult pending;
            final int pendingToken;
            synchronized (progressiveRenderLock) {
                pending = pendingProgressiveSnapshot;
                pendingToken = pendingProgressiveToken;
                pendingProgressiveSnapshot = null;
                progressiveRenderScheduled = false;
                lastProgressiveRenderAtMs = SystemClock.elapsedRealtime();
            }
            if (pending == null || !isActiveToken(pendingToken)
                    || searchInProgress || activeRenderedProfile == null) return;
            if (!sameProfile(activeRenderedProfile, pending)
                    || !normalizeHotelKey(activeRenderedProfile.hotelKey).equals(
                            normalizeHotelKey(pending.hotelKey)
                    )) return;
            final int scrollY = mainScroll == null ? 0 : mainScroll.getScrollY();
            renderProfile(pending);
            uiHandler.postDelayed(this::refreshAttachedProfileBannerAds, 160L);
            if (mainScroll != null && scrollY > 0) {
                mainScroll.post(() -> mainScroll.scrollTo(0, scrollY));
            }
        }, delay);
    }

    private void applyOfficialProfileData(ProfileResult r, JSONObject profile) {
        if (r == null || profile == null) return;
        r.officialProfile = profile;
        r.banned = false;
        JSONObject user = profile.optJSONObject("user");
        JSONObject identity = user != null ? user : profile;

        String value = firstText(
                identity, "uniqueId", "habboUniqueId", "id", "habboId"
        );
        if (!value.isEmpty()) r.uniqueId = value;
        value = firstText(identity, "name", "username", "habboName");
        if (!value.isEmpty()) r.name = value;
        value = firstText(identity, "figureString", "figure", "figure_string");
        if (!value.isEmpty()) r.figure = value;
        value = firstText(identity, "motto", "mission");
        if (!value.isEmpty()) r.motto = value;
        value = firstText(identity, "memberSince", "creationTime", "createdAt", "registeredAt");
        if (!value.isEmpty()) r.memberSince = value;
        value = firstText(identity, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
        if (!value.isEmpty()) r.lastAccess = value;
        value = firstText(identity, "currentLevel", "level");
        if (!value.isEmpty()) r.level = value;
        value = firstText(identity, "starGemCount", "starGems");
        if (!value.isEmpty()) r.starGems = value;
        value = firstText(identity, "totalBadges", "badgeCount", "badgesCount", "badgesTotal");
        if (!value.isEmpty()) r.totalBadges = value;
        if (r.badgesPagedMode && (r.officialBadgeLookup == null || r.officialBadgeLookup.isEmpty())) {
            ArrayList<JSONObject> officialBadgeList = extractList(profile, "badges");
            HashMap<String, JSONObject> lookup = new HashMap<>();
            addBadgesToLookup(lookup, officialBadgeList);
            r.officialBadgeLookup = lookup;
            if ((r.totalBadges == null || r.totalBadges.trim().isEmpty()) && !lookup.isEmpty()) {
                r.totalBadges = String.valueOf(lookup.size());
            }
            // Se a primeira página do HabboDex chegou antes do perfil oficial,
            // enriquece somente os itens já carregados, sem revarrer a coleção
            // oficial nos próximos renders.
            if (r.badgesWithAchievements != null && !r.badgesWithAchievements.isEmpty()) {
                for (JSONObject badge : r.badgesWithAchievements) {
                    if (badge == null) continue;
                    String code = firstText(badge, "code", "badgeCode");
                    if (code.isEmpty()) continue;
                    JSONObject meta = lookup.get(code.toUpperCase(Locale.ROOT));
                    if (meta != null) fillMissingJsonFields(badge, meta);
                }
                r.badges = withoutAchievementBadges(r.badgesWithAchievements);
            }
        }
        r.online = optBoolAny(identity, r.online, "online", "isOnline");
        r.privateProfile = resolveProfilePrivate(
                r.habboPublic,
                profile,
                r.dexProfile,
                r.dex
        );
    }

    private void applyComplementProfileData(ProfileResult r, JSONObject complement) {
        if (r == null || complement == null) return;
        r.dexProfile = complement;
        if (r.dex == null) r.dex = complement;
        Boolean banned = optBoolNullableDeep(complement, "isBanned", "banned", "is_banned", "ban");
        // A existência do usuário em qualquer rota oficial tem precedência:
        // perfil privado/fechado não é perfil banido.
        if (r.habboPublic != null || r.officialProfile != null) r.banned = false;
        else if (Boolean.TRUE.equals(banned)) r.banned = true;

        r.previousNames = mergeLists(
                r.previousNames,
                extractList(complement, "previousNames")
        );
        r.previousMottos = mergeLists(
                r.previousMottos,
                extractList(complement, "previousMottos")
        );
        r.oldFriends = mergeLists(
                r.oldFriends,
                extractList(complement, "previousFriends")
        );
        applyLocalStylesSource(
                r,
                mergeLists(r.allStylesSource, extractList(complement, "previousStyles"))
        );

        if (r.motto.isEmpty()) r.motto = firstText(complement, "motto", "mission");
        if (r.memberSince.isEmpty()) r.memberSince = firstText(
                complement,
                "memberSince", "creationTime", "createdAt", "registeredAt"
        );
        if (r.lastAccess.isEmpty()) r.lastAccess = firstText(
                complement,
                "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit"
        );
        if (r.level.isEmpty()) r.level = firstText(complement, "currentLevel", "level");
        if (r.starGems.isEmpty()) r.starGems = firstText(complement, "starGemCount", "starGems");
    }

    private void reconcileProfileSources(ProfileResult r) {
        if (r == null) return;
        JSONObject complement = r.dexProfile;
        JSONObject official = r.officialProfile;
        JSONObject officialUser = official == null ? null : official.optJSONObject("user");
        if (r.habboPublic != null || official != null) {
            r.banned = false;
        }
        if (official != null) {
            r.privateProfile = resolveProfilePrivate(
                    r.habboPublic,
                    official,
                    complement,
                    r.dex
            );
        }

        ArrayList<JSONObject> complementFriends = extractList(complement, "friends");
        ArrayList<JSONObject> complementRooms = extractList(complement, "rooms");
        ArrayList<JSONObject> complementGroups = extractList(complement, "groups");
        // O HabboDex só enriquece os selecionados com a data de obtenção.
        ArrayList<JSONObject> complementSelected = extractList(complement, "selectedBadges");

        if (official != null) {
            ArrayList<JSONObject> officialFriends = extractList(official, "friends");
            ArrayList<JSONObject> officialRooms = extractList(official, "rooms");
            ArrayList<JSONObject> officialGroups = extractList(official, "groups");
            ArrayList<JSONObject> officialBadges = r.badgesPagedMode
                    ? new ArrayList<>()
                    : extractList(official, "badges");
            ArrayList<JSONObject> officialSelected = extractList(officialUser, "selectedBadges");
            boolean hasOfficialSelected = officialUser != null && officialUser.has("selectedBadges");
            if (!hasOfficialSelected && r.habboPublic != null && r.habboPublic.has("selectedBadges")) {
                officialSelected = extractList(r.habboPublic, "selectedBadges");
                hasOfficialSelected = true;
            }

            if (r.privateProfile) {
                r.friends = r.friendsPagedMode
                        ? mergeListsEnrichingPrimary(r.friends, officialFriends, false)
                        : mergeListsEnrichingPrimary(officialFriends, complementFriends, true);
                r.rooms = mergeListsEnrichingPrimary(officialRooms, complementRooms, true);
                r.groups = mergeListsEnrichingPrimary(officialGroups, complementGroups, true);
                r.selectedBadges = mergeListsEnrichingPrimary(officialSelected, complementSelected, true);
                r.badgesWithAchievements = r.badgesPagedMode
                        ? mergeListsEnrichingPrimary(r.badgesWithAchievements, officialBadges, false)
                        : new ArrayList<>(officialBadges);
            } else {
                r.friends = r.friendsPagedMode
                        ? mergeListsEnrichingPrimary(r.friends, officialFriends, false)
                        : (official.has("friends")
                                ? mergeListsEnrichingPrimary(officialFriends, complementFriends, false)
                                : new ArrayList<>(complementFriends));
                r.rooms = official.has("rooms")
                        ? mergeListsEnrichingPrimary(officialRooms, complementRooms, false)
                        : new ArrayList<>(complementRooms);
                r.groups = official.has("groups")
                        ? mergeListsEnrichingPrimary(officialGroups, complementGroups, false)
                        : new ArrayList<>(complementGroups);
                r.selectedBadges = hasOfficialSelected
                        ? mergeListsEnrichingPrimary(officialSelected, complementSelected, false)
                        : new ArrayList<>(complementSelected);
                if (r.badgesPagedMode) {
                    r.badgesWithAchievements = mergeListsEnrichingPrimary(
                            r.badgesWithAchievements, officialBadges, false
                    );
                } else {
                    r.badgesWithAchievements = official.has("badges")
                            ? new ArrayList<>(officialBadges)
                            : new ArrayList<>();
                }
            }
        } else if (r.privateProfile || r.officialProfileAttempted) {
            // A fonte complementar assume dados atuais somente em perfil privado
            // ou quando a rota oficial completa realmente não respondeu.
            if (!r.friendsPagedMode) {
                r.friends = mergeListsEnrichingPrimary(r.friends, complementFriends, true);
            }
            r.rooms = mergeListsEnrichingPrimary(r.rooms, complementRooms, true);
            r.groups = mergeListsEnrichingPrimary(r.groups, complementGroups, true);
            r.selectedBadges = mergeListsEnrichingPrimary(r.selectedBadges, complementSelected, true);
            // Emblemas paginados já carregados pelo HabboDex permanecem soberanos.
        }

        r.badges = withoutAchievementBadges(r.badgesWithAchievements);
        if (r.badgesWithAchievements != null && !r.badgesWithAchievements.isEmpty()) {
            int declared = 0;
            try { declared = Integer.parseInt(r.totalBadges); } catch(Exception ignored) {}
            int pagedTotal = Math.max(r.badgesTotal, r.badgesWithAchievements.size());
            r.totalBadges = String.valueOf(Math.max(declared, pagedTotal));
        }
        sortProfileChronologyNewestFirst(r);

        if (r.banned) {
            r.online = false;
            r.privateProfile = false;
        }

        if (r.officialPhotosAttempted) {
            ArrayList<JSONObject> complementPhotos = extractList(complement, "photos");
            // Fotos do complemento são exclusivas de perfis fechados/banidos.
            // Em perfil público, até uma lista oficial vazia continua soberana.
            boolean restrictedPhotos = r.privateProfile || r.banned;
            if (restrictedPhotos
                    && (!r.officialPhotosSucceeded || r.allPhotosSource.isEmpty())) {
                if (!complementPhotos.isEmpty()) applyLocalPhotosSource(r, complementPhotos, false);
            }
        }
    }

    private void sortProfileChronologyNewestFirst(ProfileResult profile) {
        if (profile == null) return;
        sortJsonNewestFirstMissingFirst(
                profile.friends,
                "creationTime", "friendSince", "addedAt", "createdAt", "date"
        );
        sortJsonNewestFirstMissingFirst(
                profile.oldFriends,
                "removedAt", "leftAt", "date", "creationTime", "friendSince", "createdAt"
        );
        sortJsonNewestFirst(
                profile.rooms,
                "creationTime", "createdAt", "date", "updatedAt"
        );
        // Emblemas chegam paginados pelo HabboDex, já na ordem da fonte. Não
        // reordenamos toda a coleção a cada página para manter paginação estável
        // e evitar trabalho extra em aparelhos mais fracos.
    }

    private void sortJsonNewestFirst(ArrayList<JSONObject> items, String... dateKeys) {
        if (items == null || items.size() < 2) return;
        Collections.sort(items, (left, right) -> {
            long leftTime = jsonDateMillis(left, dateKeys);
            long rightTime = jsonDateMillis(right, dateKeys);
            return Long.compare(rightTime, leftTime);
        });
    }

    private void sortJsonNewestFirstMissingFirst(ArrayList<JSONObject> items, String... dateKeys) {
        if (items == null || items.size() < 2) return;
        Collections.sort(items, (left, right) -> {
            long leftTime = jsonDateMillis(left, dateKeys);
            long rightTime = jsonDateMillis(right, dateKeys);
            boolean leftMissing = leftTime <= 0L;
            boolean rightMissing = rightTime <= 0L;
            if (leftMissing != rightMissing) return leftMissing ? -1 : 1;
            if (leftMissing) return 0;
            return Long.compare(rightTime, leftTime);
        });
    }

    private long jsonDateMillis(JSONObject item, String... dateKeys) {
        if (item == null) return 0L;
        if (dateKeys != null) {
            for (String key : dateKeys) {
                Date parsed = parseHabboDate(item.optString(key, ""));
                if (parsed != null) return parsed.getTime();
            }
        }
        return 0L;
    }

    private void applyOfficialPhotosData(
            ProfileResult r,
            ArrayList<JSONObject> photos,
            boolean success
    ) {
        if (r == null) return;
        r.officialPhotosAttempted = true;
        r.officialPhotosSucceeded = success;
        if (success) applyLocalPhotosSource(r, photos, true);
    }

    private void applyLocalPhotosSource(
            ProfileResult r,
            ArrayList<JSONObject> source,
            boolean official
    ) {
        if (r == null) return;
        r.allPhotosSource = source == null ? new ArrayList<>() : new ArrayList<>(source);
        r.photosFromOfficial = official;
        int end = Math.min(PAGE_CHUNK, r.allPhotosSource.size());
        r.photos = new ArrayList<>(r.allPhotosSource.subList(0, end));
        r.photosTotal = r.allPhotosSource.size();
        r.photosHasMore = end < r.photosTotal;
        r.photosNextPage = r.photosHasMore ? 2 : 0;
    }

    private void applyLocalStylesSource(ProfileResult r, ArrayList<JSONObject> source) {
        if (r == null) return;
        ArrayList<JSONObject> clean = source == null ? new ArrayList<>() : new ArrayList<>(source);
        if (r.stylesRemotePaged) {
            r.allStylesSource = mergeLists(r.allStylesSource, clean);
            r.stylesFromComplement = false;
            if (r.previousStyles == null || r.previousStyles.isEmpty()) {
                int end = Math.min(PAGE_CHUNK, r.allStylesSource.size());
                r.previousStyles = new ArrayList<>(r.allStylesSource.subList(0, end));
            }
            r.stylesTotal = Math.max(r.stylesTotal, r.allStylesSource.size());
            r.stylesHasMore = r.previousStyles.size() < r.allStylesSource.size()
                    || r.stylesRemotePaged;
            return;
        }
        r.allStylesSource = clean;
        r.stylesFromComplement = true;
        int end = Math.min(PAGE_CHUNK, r.allStylesSource.size());
        r.previousStyles = new ArrayList<>(r.allStylesSource.subList(0, end));
        r.stylesTotal = r.allStylesSource.size();
        r.stylesHasMore = end < r.stylesTotal;
        r.stylesNextPage = r.stylesHasMore ? 2 : 0;
    }

    private boolean isAchievementBadge(JSONObject item) {
        if (item == null) return false;
        String code = firstText(item, "code", "badgeCode").trim().toUpperCase(Locale.ROOT);
        // No Habbo, conquistas usam códigos ACH_. Nem toda resposta do HabboDex
        // preenche isAchievement/achievement, então o prefixo é a fonte primária.
        return code.startsWith("ACH_")
                || optBoolAny(item, false, "isAchievement", "achievement");
    }

    private ArrayList<JSONObject> withoutAchievementBadges(ArrayList<JSONObject> source) {
        ArrayList<JSONObject> out = new ArrayList<>();
        if (source == null) return out;
        for (JSONObject item : source) {
            if (!isAchievementBadge(item)) out.add(item);
        }
        return out;
    }

    private ArrayList<JSONObject> mergeListsEnrichingPrimary(
            ArrayList<JSONObject> primary,
            ArrayList<JSONObject> supplement,
            boolean appendMissing
    ) {
        ArrayList<JSONObject> out = new ArrayList<>();
        HashMap<String, JSONObject> byKey = new HashMap<>();
        if (primary != null) {
            for (JSONObject item : primary) {
                if (item == null) continue;
                out.add(item);
                for (String key : matchingItemKeys(item)) byKey.put(key, item);
            }
        }
        if (supplement != null) {
            for (JSONObject item : supplement) {
                if (item == null) continue;
                ArrayList<String> keys = matchingItemKeys(item);
                JSONObject target = null;
                for (String key : keys) {
                    target = byKey.get(key);
                    if (target != null) break;
                }
                if (target != null) {
                    fillMissingJsonFields(target, item);
                    for (String key : matchingItemKeys(target)) byKey.put(key, target);
                } else if (appendMissing) {
                    out.add(item);
                    for (String key : keys) byKey.put(key, item);
                }
            }
        }
        return out;
    }

    private ArrayList<String> matchingItemKeys(JSONObject item) {
        ArrayList<String> keys = new ArrayList<>();
        if (item == null) return keys;
        String id = normalizeNickKey(habboUniqueIdFromRecord(item));
        String badge = normalizeNickKey(firstText(item, "badgeCode", "code"));
        String name = normalizeNickKey(firstText(
                item, "name", "username", "habboName", "nickname"
        ));
        if (!id.isEmpty()) keys.add("id:" + id);
        if (!badge.isEmpty()) keys.add("badge:" + badge);
        if (!name.isEmpty()) keys.add("name:" + name);
        if (keys.isEmpty()) keys.add(stableItemKey(item));
        return keys;
    }

    private void fillMissingJsonFields(JSONObject target, JSONObject supplement) {
        if (target == null || supplement == null) return;
        Iterator<String> keys = supplement.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object extra = supplement.opt(key);
            Object current = target.opt(key);
            try {
                if (isMissingJsonValue(current)) {
                    target.put(key, extra);
                } else if (current instanceof JSONObject && extra instanceof JSONObject) {
                    fillMissingJsonFields((JSONObject) current, (JSONObject) extra);
                }
            } catch(Exception ignored) {}
        }
    }

    private boolean isMissingJsonValue(Object value) {
        if (value == null || value == JSONObject.NULL) return true;
        if (value instanceof String) {
            String text = ((String) value).trim();
            return text.isEmpty() || "null".equalsIgnoreCase(text);
        }
        if (value instanceof JSONArray) return ((JSONArray) value).length() == 0;
        return false;
    }

    private String habboUniqueIdFromRecord(JSONObject item) {
        if (item == null) return "";
        String[] keys = new String[]{
                "uniqueId", "habboUniqueId", "habboId", "userId", "id"
        };
        for (String key : keys) {
            String candidate = item.optString(key, "").trim();
            if (candidate.matches("(?i)^hh[a-z]{2}-[a-z0-9]+$")) {
                return candidate;
            }
        }
        String[] wrappers = new String[]{"user", "habbo", "profile", "friend"};
        for (String wrapper : wrappers) {
            JSONObject nested = item.optJSONObject(wrapper);
            if (nested == null || nested == item) continue;
            String candidate = habboUniqueIdFromRecord(nested);
            if (!candidate.isEmpty()) return candidate;
        }
        return "";
    }

    private void enrichSelectedBadgesWithOwnership(ProfileResult r) {
        if (r == null || r.selectedBadges == null || r.selectedBadges.isEmpty()) return;
        HashMap<String, JSONObject> byCode = new HashMap<>();
        addBadgesToLookup(byCode, r.badges);
        addBadgesToLookup(byCode, r.badgesWithAchievements);
        for (JSONObject selected : r.selectedBadges) {
            if (selected == null) continue;
            String code = firstText(selected, "code", "badgeCode");
            if (code.isEmpty()) continue;
            JSONObject full = byCode.get(code.toUpperCase(Locale.ROOT));
            if (full == null) continue;
            try {
                if (!selected.has("totalOwners") && full.has("totalOwners")) selected.put("totalOwners", full.opt("totalOwners"));
                if (firstText(selected, "name", "title").isEmpty() && !firstText(full, "name", "title").isEmpty()) selected.put("name", firstText(full, "name", "title"));
                if (firstText(selected, "description", "desc").isEmpty() && !firstText(full, "description", "desc").isEmpty()) selected.put("description", firstText(full, "description", "desc"));
                String obtained = firstText(
                        full,
                        "obtainedAt", "acquiredAt", "creationTime", "createdAt", "date"
                );
                if (firstText(selected, "obtainedAt", "acquiredAt", "creationTime", "createdAt", "date").isEmpty() && !obtained.isEmpty()) {
                    selected.put("creationTime", obtained);
                }
            } catch(Exception ignored) {}
        }
    }

    private void addBadgesToLookup(HashMap<String, JSONObject> byCode, ArrayList<JSONObject> list) {
        if (byCode == null || list == null) return;
        for (JSONObject b : list) {
            if (b == null) continue;
            String code = firstText(b, "code", "badgeCode");
            if (!code.isEmpty()) byCode.put(code.toUpperCase(Locale.ROOT), b);
        }
    }

    private ArrayList<JSONObject> fetchAll(String uniqueId, String endpoint, String primaryKey, int limit, int maxPages) {
        ArrayList<JSONObject> out = new ArrayList<>();
        int page = 1;
        for (int i = 0; i < maxPages; i++) {
            try {
                JSONObject pageData = unwrap(getJson(habbodexEndpointUrl(uniqueId, endpoint, page, limit)));
                if (pageData == null) break;
                ArrayList<JSONObject> items = extractList(pageData, primaryKey);
                if (items.isEmpty()) break;
                out.addAll(items);
                JSONObject next = pageData.optJSONObject("next");
                int nextPage = next == null ? 0 : next.optInt("page", 0);
                if (nextPage <= 0) {
                    int totalPages = pageData.optInt("totalPages", pageData.optInt("pages", 0));
                    JSONObject pagination = pageData.optJSONObject("pagination");
                    if (pagination != null) {
                        totalPages = Math.max(totalPages, pagination.optInt("totalPages", pagination.optInt("pages", 0)));
                        nextPage = pagination.optInt("nextPage", 0);
                    }
                    if (nextPage <= 0 && totalPages > page) nextPage = page + 1;
                    if (nextPage <= 0 && items.size() >= limit) nextPage = page + 1;
                }
                if (nextPage <= 0 || nextPage == page || nextPage > maxPages) break;
                page = nextPage;
            } catch (Exception ignored) { break; }
        }
        return out;
    }



    private boolean allBadgesHaveObtainedDates(ArrayList<JSONObject> list) {
        if (list == null || list.isEmpty()) return true;
        for (JSONObject badge : list) {
            if (badge == null) continue;
            String code = firstText(badge, "code", "badgeCode");
            if (code.isEmpty()) continue;
            if (badgeObtainedDate(badge).isEmpty()) return false;
        }
        return true;
    }

    private PageResult fetchBadgesPageIncludingAchievements(
            String uniqueId,
            int page,
            int limit
    ) {
        PageResult out = new PageResult();
        out.page = Math.max(1, page);
        out.nextPage = 0;
        out.hasMore = false;
        out.total = 0;
        try {
            // Sempre baixa o lote completo. "Ocultar conquistas" é somente um
            // filtro local de UI; assim desativar o botão não depende de uma
            // segunda consulta e nunca fica preso a uma página já filtrada.
            String url = habbodexListUrl(uniqueId, "badges", out.page, limit)
                    + "&hideAchievements=false";
            JSONObject pageData = unwrap(getJson(url));
            if (pageData == null) return out;
            out.success = true;
            out.items = extractList(pageData, "badges");
            if (out.items.isEmpty()) out.items = extractList(pageData, "result");
            if (out.items.isEmpty()) out.items = extractList(pageData, null);
            out.total = extractTotalCount(pageData);

            JSONObject next = pageData.optJSONObject("next");
            int nextPage = next == null ? 0 : next.optInt("page", 0);
            if (nextPage <= 0) {
                JSONObject pagination = pageData.optJSONObject("pagination");
                if (pagination != null) {
                    nextPage = pagination.optInt("nextPage", 0);
                }
            }
            if (nextPage <= 0) {
                int totalPages = pageData.optInt("totalPages", pageData.optInt("pages", 0));
                JSONObject pagination = pageData.optJSONObject("pagination");
                if (pagination != null) {
                    totalPages = Math.max(
                            totalPages,
                            pagination.optInt("totalPages", pagination.optInt("pages", 0))
                    );
                }
                if (totalPages > out.page) nextPage = out.page + 1;
            }
            if (nextPage <= 0 && out.items.size() >= limit) {
                nextPage = out.page + 1;
            }
            out.nextPage = nextPage > out.page ? nextPage : 0;
            out.hasMore = out.nextPage > 0;
        } catch(Exception ignored) {}
        return out;
    }

    private PageResult fetchCriticalBadgesPage(
            String uniqueId,
            int page,
            int limit,
            boolean initial
    ) {
        PageResult best = null;
        int attempts = initial ? 3 : 2;
        String url = habbodexListUrl(uniqueId, "badges", page, limit)
                + "&hideAchievements=false";
        for (int attempt = 0; attempt < attempts; attempt++) {
            if (attempt > 0) {
                invalidateFiveMinuteJsonCache(url);
                sleepCriticalRetry(attempt);
            }
            PageResult candidate = fetchBadgesPageIncludingAchievements(
                    uniqueId, page, limit
            );
            if (candidate != null && candidate.success) {
                best = candidate;
                boolean empty = candidate.items == null || candidate.items.isEmpty();
                if (empty) {
                    // Um 200 vazio pode ser transitório no WebView/HabboDex. Só
                    // aceita vazio depois de esgotar as tentativas frescas.
                    if (attempt >= attempts - 1) return candidate;
                    continue;
                }
                if (allBadgesHaveObtainedDates(candidate.items)) return candidate;
            }
        }
        return best == null ? new PageResult() : best;
    }


    private void applyBadgesPage(ProfileResult r, PageResult page, boolean replace) {
        if (r == null || page == null || !page.success) return;
        ArrayList<JSONObject> incoming = page.items == null
                ? new ArrayList<>()
                : new ArrayList<>(page.items);
        // Enriquecimento O(100): consulta somente os códigos desta página no
        // índice oficial criado uma vez, em vez de percorrer milhares de badges.
        if (r.officialBadgeLookup != null && !r.officialBadgeLookup.isEmpty()) {
            for (JSONObject badge : incoming) {
                if (badge == null) continue;
                String code = firstText(badge, "code", "badgeCode");
                if (code.isEmpty()) continue;
                JSONObject officialMeta = r.officialBadgeLookup.get(code.toUpperCase(Locale.ROOT));
                if (officialMeta != null) fillMissingJsonFields(badge, officialMeta);
            }
        }
        r.badgesPagedMode = true;
        r.badgesWithAchievements = replace
                ? incoming
                : mergeListsEnrichingPrimary(r.badgesWithAchievements, incoming, true);
        r.badges = withoutAchievementBadges(r.badgesWithAchievements);

        int declared = 0;
        try { declared = Integer.parseInt(r.totalBadges); } catch(Exception ignored) {}
        int officialCount = r.officialBadgeLookup == null ? 0 : r.officialBadgeLookup.size();
        r.badgesTotal = Math.max(
                Math.max(r.badgesTotal, page.total),
                Math.max(declared, Math.max(officialCount, r.badgesWithAchievements.size()))
        );
        if (r.badgesTotal > 0) r.totalBadges = String.valueOf(r.badgesTotal);
        r.badgesNextPage = page.nextPage;
        r.badgesHasMore = page.hasMore
                || (r.badgesTotal > 0 && r.badgesWithAchievements.size() < r.badgesTotal);
        if (r.badgesHasMore && r.badgesNextPage <= page.page) {
            r.badgesNextPage = page.page + 1;
        }
        enrichSelectedBadgesWithOwnership(r);
    }

    private PageResult fetchPage(String uniqueId, String endpoint, String primaryKey, int page, int limit) {
        PageResult out = new PageResult();
        out.page = Math.max(1, page);
        out.nextPage = 0;
        out.hasMore = false;
        out.total = 0;
        try {
            JSONObject pageData = unwrap(getJson(habbodexEndpointUrl(uniqueId, endpoint, out.page, limit)));
            if (pageData == null) return out;
            out.success = true;
            out.items = extractList(pageData, primaryKey);
            if (out.items.isEmpty() && "previousFriends".equals(primaryKey)) {
                out.items = extractList(pageData, "friends");
            } else if (out.items.isEmpty() && "previousStyles".equals(primaryKey)) {
                out.items = extractList(pageData, "styles");
            } else if (out.items.isEmpty() && "previousMottos".equals(primaryKey)) {
                out.items = extractList(pageData, "mottos");
            } else if (out.items.isEmpty() && "previousNames".equals(primaryKey)) {
                out.items = extractList(pageData, "names");
            }
            out.total = extractTotalCount(pageData);
            JSONObject next = pageData.optJSONObject("next");
            int nextPage = next == null ? 0 : next.optInt("page", 0);
            if (nextPage <= 0) {
                JSONObject pagination = pageData.optJSONObject("pagination");
                if (pagination != null) nextPage = pagination.optInt("nextPage", 0);
            }
            if (nextPage <= 0) {
                int totalPages = pageData.optInt("totalPages", pageData.optInt("pages", 0));
                JSONObject pagination = pageData.optJSONObject("pagination");
                if (pagination != null) totalPages = Math.max(totalPages, pagination.optInt("totalPages", pagination.optInt("pages", 0)));
                if (totalPages > out.page) nextPage = out.page + 1;
            }
            if (nextPage <= 0 && out.items.size() >= limit) nextPage = out.page + 1;
            out.nextPage = nextPage > out.page ? nextPage : 0;
            out.hasMore = out.nextPage > 0;
        } catch (Exception ignored) {}
        return out;
    }

    private PageResult fetchPageChunk(String uniqueId, String endpoint, String primaryKey, int startPage, int pageLimit, int desiredCount) {
        PageResult combined = new PageResult();
        combined.page = Math.max(1, startPage);
        combined.nextPage = 0;
        combined.hasMore = false;
        combined.total = 0;

        int page = combined.page;
        int safety = 0;
        int target = Math.max(1, desiredCount);
        int limit = Math.max(1, pageLimit);

        while (page > 0 && safety < 12 && combined.items.size() < target) {
            PageResult part = fetchPage(uniqueId, endpoint, primaryKey, page, limit);
            if (part == null) break;
            if (combined.total <= 0 && part.total > 0) combined.total = part.total;
            if (part.items == null || part.items.isEmpty()) {
                combined.nextPage = 0;
                combined.hasMore = false;
                break;
            }
            for (JSONObject item : part.items) {
                if (combined.items.size() >= target) break;
                combined.items.add(item);
            }
            combined.page = part.page;
            if (part.nextPage <= page || !part.hasMore) {
                combined.nextPage = 0;
                combined.hasMore = false;
                break;
            }
            page = part.nextPage;
            combined.nextPage = page;
            combined.hasMore = true;
            safety++;
        }

        if (combined.total > 0 && combined.items.size() < Math.min(target, combined.total) && combined.nextPage <= 0) {
            combined.nextPage = Math.max(startPage + 1, page + 1);
            combined.hasMore = true;
        }
        if (combined.total > 0 && combined.items.size() >= combined.total) {
            combined.nextPage = 0;
            combined.hasMore = false;
        }
        return combined;
    }

    private int extractTotalCount(JSONObject data) {
        if (data == null) return 0;
        int total = firstPositiveInt(data, "total", "totalItems", "totalCount", "count", "recordsTotal");
        JSONObject pagination = data.optJSONObject("pagination");
        if (total <= 0 && pagination != null) total = firstPositiveInt(pagination, "total", "totalItems", "totalCount", "count");
        JSONObject meta = data.optJSONObject("meta");
        if (total <= 0 && meta != null) total = firstPositiveInt(meta, "total", "totalItems", "totalCount", "count");
        return total;
    }

    private int firstPositiveInt(JSONObject data, String... keys) {
        if (data == null || keys == null) return 0;
        for (String key : keys) {
            if (data.has(key)) {
                int v = data.optInt(key, 0);
                if (v > 0) return v;
            }
        }
        return 0;
    }

    private int extractBatchSectionTotal(JSONObject batch, String key) {
        if (batch == null || key == null || key.isEmpty()) return 0;
        JSONObject totals = batch.optJSONObject("totals");
        if (totals == null) return 0;
        return Math.max(0, totals.optInt(key, 0));
    }

    private void applyPhotosPage(ProfileResult r, PageResult page, boolean reset) {
        if (r == null || page == null) return;
        if (reset) r.photos.clear();
        r.photos = mergeLists(r.photos, page.items);
        if (page.total > 0) r.photosTotal = page.total;
        int total = r.photosTotal > 0 ? r.photosTotal : page.total;
        r.photosHasMore = page.hasMore || (total > 0 && r.photos.size() < total);
        r.photosNextPage = page.nextPage;
        if (r.photosHasMore && r.photosNextPage <= 0) r.photosNextPage = Math.max(2, page.page + 1);
        if (!r.photosHasMore) r.photosNextPage = 0;
    }

    private void applyStylesPage(ProfileResult r, PageResult page, boolean reset) {
        if (r == null || page == null) return;
        if (reset) r.previousStyles.clear();
        r.previousStyles = mergeLists(r.previousStyles, page.items);
        if (page.total > 0) r.stylesTotal = page.total;
        int total = r.stylesTotal > 0 ? r.stylesTotal : page.total;
        r.stylesHasMore = page.hasMore || (total > 0 && r.previousStyles.size() < total);
        r.stylesNextPage = page.nextPage;
        if (r.stylesHasMore && r.stylesNextPage <= 0) r.stylesNextPage = Math.max(2, page.page + 1);
        if (!r.stylesHasMore) r.stylesNextPage = 0;
    }

    private void loadMorePhotos(ProfileResult r, HorizontalScrollView photosHsv) {
        if (r == null || r.photosLoading || !r.photosHasMore || r.uniqueId == null || r.uniqueId.isEmpty()) return;
        if (r.allPhotosSource != null && !r.allPhotosSource.isEmpty()) {
            photosScrollX = photosHsv == null ? 0 : photosHsv.getScrollX();
            int end = Math.min(r.photos.size() + PAGE_CHUNK, r.allPhotosSource.size());
            r.photos = new ArrayList<>(r.allPhotosSource.subList(0, end));
            r.photosTotal = r.allPhotosSource.size();
            r.photosHasMore = end < r.photosTotal;
            r.photosNextPage = r.photosHasMore ? (end / PAGE_CHUNK) + 1 : 0;
            enrichPhotoRoomInfo(r);
            renderProfile(r);
            return;
        }
        final int token = activeSearchToken;
        final int page = r.photosNextPage <= 0 ? 2 : r.photosNextPage;
        r.photosLoading = true;
        photosScrollX = photosHsv == null ? 0 : photosHsv.getScrollX();
        renderProfile(r);
        executor.execute(() -> {
            try {
                PageResult next = fetchPageChunk(r.uniqueId, "photos", "photos", page, PAGE_CHUNK, PAGE_CHUNK);
                if (!isActiveToken(token)) return;
                applyPhotosPage(r, next, false);
                try { enrichPhotoRoomInfo(r); } catch(Exception ignored) {}
            } catch (Exception ignored) {
            } finally {
                r.photosLoading = false;
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    renderProfile(r);
                });
            }
        });
    }

    private void loadMoreStyles(ProfileResult r, HorizontalScrollView stylesHsv) {
        if (r == null || r.stylesLoading || !r.stylesHasMore
                || r.uniqueId == null || r.uniqueId.isEmpty()) return;

        stylesScrollX = stylesHsv == null ? 0 : stylesHsv.getScrollX();

        // Primeiro libera itens que já vieram na página de rede atual.
        if (r.allStylesSource != null
                && r.previousStyles.size() < r.allStylesSource.size()) {
            int end = Math.min(
                    r.previousStyles.size() + PAGE_CHUNK,
                    r.allStylesSource.size()
            );
            r.previousStyles = new ArrayList<>(r.allStylesSource.subList(0, end));
            r.stylesHasMore = end < r.allStylesSource.size() || r.stylesRemotePaged;
            if (!r.stylesHasMore) r.stylesNextPage = 0;
            renderProfile(r);
            return;
        }

        // Quando os 100 itens já baixados foram consumidos, busca a próxima
        // página remota de 100 e volta a liberar em blocos de PAGE_CHUNK.
        if (r.stylesRemotePaged) {
            final int token = activeSearchToken;
            final int remotePage = r.stylesRemoteNextPage <= 1
                    ? 2
                    : r.stylesRemoteNextPage;
            r.stylesLoading = true;
            renderProfile(r);
            executor.execute(() -> {
                try {
                    PageResult next = fetchPage(
                            r.uniqueId,
                            "previous-styles",
                            "previousStyles",
                            remotePage,
                            100
                    );
                    if (!isActiveToken(token) || !next.success) return;
                    synchronized (r) {
                        r.allStylesSource = mergeLists(r.allStylesSource, next.items);
                        if (next.total > 0) {
                            r.stylesTotal = Math.max(r.stylesTotal, next.total);
                        }
                        r.stylesRemoteNextPage = next.nextPage;
                        r.stylesRemotePaged = next.hasMore
                                || (r.stylesTotal > 0
                                && r.allStylesSource.size() < r.stylesTotal);
                        if (r.stylesRemotePaged
                                && r.stylesRemoteNextPage <= remotePage) {
                            r.stylesRemoteNextPage = remotePage + 1;
                        }
                        int end = Math.min(
                                r.previousStyles.size() + PAGE_CHUNK,
                                r.allStylesSource.size()
                        );
                        r.previousStyles = new ArrayList<>(
                                r.allStylesSource.subList(0, end)
                        );
                        r.stylesHasMore = end < r.allStylesSource.size()
                                || r.stylesRemotePaged;
                        if (!r.stylesHasMore) r.stylesNextPage = 0;
                    }
                } catch(Exception ignored) {
                } finally {
                    r.stylesLoading = false;
                    runOnUiThread(() -> {
                        if (!isActiveToken(token)) return;
                        renderProfile(r);
                    });
                }
            });
            return;
        }

        // Compatibilidade com fontes locais completas já existentes.
        if (r.stylesFromComplement && r.allStylesSource != null) {
            int end = Math.min(
                    r.previousStyles.size() + PAGE_CHUNK,
                    r.allStylesSource.size()
            );
            r.previousStyles = new ArrayList<>(r.allStylesSource.subList(0, end));
            r.stylesHasMore = end < r.allStylesSource.size();
            r.stylesNextPage = r.stylesHasMore ? (end / PAGE_CHUNK) + 1 : 0;
            renderProfile(r);
        }
    }

    private ArrayList<JSONObject> fetchOfficialPhotos(String uniqueId) throws Exception {
        ArrayList<JSONObject> out = new ArrayList<>();
        if (uniqueId == null || uniqueId.trim().isEmpty()) return out;
        Object data = getJsonAny(
                habboApiUrl("/extradata/public/users/" + enc(uniqueId) + "/photos")
        );
        if (data instanceof JSONArray) {
            JSONArray a = (JSONArray)data;
            for (int i=0; i<a.length(); i++) {
                JSONObject o = a.optJSONObject(i);
                if (o != null) out.add(o);
            }
        } else if (data instanceof JSONObject) {
            out.addAll(extractList((JSONObject)data, null));
        }
        return out;
    }

    private void renderProfile(ProfileResult r) {
        loadingSkeletonProgressBar = null;
        String renderedHotel = r == null ? "" : normalizeHotelKey(r.hotelKey);
        if (!renderedHotel.isEmpty() && !renderedHotel.equals(currentHotelKey)) {
            currentHotelKey = renderedHotel;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(PREF_HOTEL, currentHotelKey)
                    .apply();
        }
        updateSelectedHotelHeaderFlag();
        normalizeProfileState(r);
        activeRenderedProfile = r;
        startScreenVisible = false;
        updateStartNativeAdVisibility();
        rememberOpenedProfile(r);
        currentProfilePrivate = r != null && (r.privateProfile || r.banned);
        profileAvatarTutorialTarget = null;
        profileFavoriteTutorialTarget = null;
        profileFriendTutorialTarget = null;
        if (!searchInProgress) setLoading(false, "");
        resultWrap.removeAllViews();

        if (profileSectionsInProgress
                && inlineProgressMessage != null
                && !inlineProgressMessage.trim().isEmpty()) {
            resultWrap.addView(loadingProgressCard(inlineProgressMessage, inlineProgressPct), lp(-1, -2, 0, 0, 0, 12));
        }

        LinearLayout profile = card(dp(26));
        applyProfilePrivateBorder(profile, dp(26));
        profile.setPadding(dp(20), dp(20), dp(20), dp(20));
        if (Build.VERSION.SDK_INT >= 21) profile.setElevation(dp(5));
        resultWrap.addView(profile, lp(-1, -2, 0, 0, 0, 16));

        FrameLayout avatarFrame = new FrameLayout(this);
        avatarFrame.setBackground(round(
                lightTheme ? Color.rgb(246,244,250) : Color.rgb(13, 12, 20),
                dp(22),
                lightTheme ? Color.rgb(219,213,230) : Color.rgb(60, 52, 78),
                1
        ));
        profile.addView(avatarFrame, lp(-1, dp(220), 0, 0, 0, 16));
        ImageView avatar = new ImageView(this);
        avatar.setAdjustViewBounds(true);
        avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        avatar.setPadding(dp(20), dp(8), dp(20), dp(8));
        avatarFrame.addView(avatar, new FrameLayout.LayoutParams(-1, -1));
        currentAvatarImage = avatar;
        profileAvatarTutorialTarget = avatarFrame;

        TextView favoriteStar = text("", 22, Color.WHITE, true);
        favoriteStar.setGravity(Gravity.CENTER);
        favoriteStar.setPadding(0, 0, 0, 0);
        favoriteStar.setBackground(new FavoriteStarDrawable(isFavoriteProfile(r)));
        FrameLayout.LayoutParams favoriteStarLp = new FrameLayout.LayoutParams(dp(42), dp(42), Gravity.TOP | Gravity.RIGHT);
        favoriteStarLp.topMargin = dp(10);
        favoriteStarLp.rightMargin = dp(10);
        avatarFrame.addView(favoriteStar, favoriteStarLp);
        profileFavoriteTutorialTarget = favoriteStar;
        favoriteStar.setOnClickListener(v -> {
            toggleFavoriteProfile(r);
            favoriteStar.setBackground(new FavoriteStarDrawable(isFavoriteProfile(r)));
        });
        String nextAvatarProfileKey = profileIdentityKey(r.hotelKey, r.uniqueId, r.name);
        if (!nextAvatarProfileKey.equals(currentAvatarProfileKey)) avatarDirection = 2;
        currentAvatarProfileKey = nextAvatarProfileKey;
        currentProfileFigure = r.figure;
        updateProfileAvatar();
        bindProfileAvatarGestures(avatar, r.figure);

        TextView name = habboText(r.name, 30, true);
        name.setGravity(Gravity.CENTER);
        name.setLetterSpacing(0.012f);
        profile.addView(name, lp(-1, -2, 0, 0, 0, 10));
        if (!r.motto.isEmpty()) {
            TextView motto = habboText(r.motto, 16, false);
            motto.setGravity(Gravity.CENTER);
            motto.setTextColor(lightTheme ? Color.rgb(70,70,70) : Color.argb(220,255,255,255));
            motto.setLineSpacing(dp(2), 1f);
            profile.addView(motto, lp(-1, -2, 0, 0, 0, 14));
        }
        LinearLayout badges = new LinearLayout(this);
        badges.setGravity(Gravity.CENTER);
        badges.setOrientation(LinearLayout.HORIZONTAL);
        profile.addView(badges, lp(-1, -2, 0, 0, 0, 6));
        if (r.banned) {
            badges.addView(profileBadge(t(R.string.profile_banned), "ban", red));
        } else if (r.privateProfile) {
            badges.addView(profileBadge(t(R.string.profile_private), "lock", red));
        }

        addSelectedBadges(r.selectedBadges);
        addPreviousNames(r.previousNames);
        addPreviousMottos(r, r.previousMottos);
        addPreviousStyles(r);
        addPhotos(r);
        addStats(r);
        addFriendsTabs(r);
        addRoomsTabs(r.rooms);
        addGroups(r.groups);
        addBadgesSection(r);
    }

    private LinearLayout profileBadge(String label, String icon, int color) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setPadding(dp(8), dp(5), dp(10), dp(5));
        row.setBackground(round(adjustAlpha(color, 0.32f), dp(999), adjustAlpha(color, 0.55f), 1));
        View badgeIcon = new IconView(this, icon);
        row.addView(badgeIcon, new LinearLayout.LayoutParams(dp(14), dp(14)));
        TextView tv = text(label, 13, Color.WHITE, true);
        tv.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-2, -2); tp.leftMargin = dp(6);
        row.addView(tv, tp);
        return row;
    }

    private TextView roundIconButton(String label) {
        TextView v = text("", 19, Color.WHITE, true);
        v.setGravity(Gravity.CENTER);
        v.setIncludeFontPadding(false);
        v.setElevation(dp(3));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(38), dp(34));
        p.setMargins(dp(4), 0, dp(4), 0);
        v.setLayoutParams(p);
        if ("shirt".equals(label)) {
            v.setBackground(new ShirtDrawable());
        } else {
            v.setBackground(new ArrowButtonDrawable("‹".equals(label)));
        }
        return v;
    }

    private void updateProfileAvatar() {
        if (currentAvatarImage != null && currentProfileFigure != null && !currentProfileFigure.isEmpty()) {
            loadAvatarImageKeepingCurrent(currentAvatarImage, avatarFull(currentProfileFigure, avatarDirection));
        }
    }

    private void bindProfileAvatarGestures(final ImageView avatar, final String figure) {
        if (avatar == null) return;
        final float[] downX = {0f};
        final float[] downY = {0f};
        final boolean[] swipeConsumed = {false};
        final Runnable[] holdTask = new Runnable[1];

        avatar.setClickable(true);
        avatar.setLongClickable(true);
        avatar.setContentDescription(t(R.string.profile_tutorial_looks_body));
        avatar.setOnLongClickListener(v -> {
            showClothesDialog(figure, t(R.string.current_look));
            return true;
        });
        avatar.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                requestDisallowParents(v, true);
                downX[0] = event.getX();
                downY[0] = event.getY();
                swipeConsumed[0] = false;
                holdTask[0] = () -> {
                    if (swipeConsumed[0]) return;
                    try { v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS); } catch (Exception ignored) {}
                    v.performLongClick();
                };
                uiHandler.postDelayed(holdTask[0], 550L);
                return true;
            }
            if (action == MotionEvent.ACTION_MOVE) {
                float totalDx = event.getX() - downX[0];
                float totalDy = event.getY() - downY[0];
                if (Math.abs(totalDx) > dp(12) || Math.abs(totalDy) > dp(12)) {
                    if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                }
                if (!swipeConsumed[0] && Math.abs(totalDx) >= dp(34) && Math.abs(totalDx) > Math.abs(totalDy)) {
                    avatarDirection = normalizeDirection(avatarDirection + (totalDx > 0 ? 1 : -1));
                    swipeConsumed[0] = true;
                    updateProfileAvatar();
                }
                return true;
            }
            if (action == MotionEvent.ACTION_UP) {
                if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                requestDisallowParents(v, false);
                return true;
            }
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE) {
                if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                requestDisallowParents(v, false);
                return true;
            }
            return true;
        });
    }

    private int normalizeDirection(int value) {
        while (value < 0) value += 8;
        while (value > 7) value -= 8;
        return value;
    }


    private void addStats(ProfileResult r) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        resultWrap.addView(wrap, lp(-1, -2, 0, 0, 0, 18));
        wrap.addView(statRow(r.online ? "status_online" : "status_offline", t(R.string.status), r.online ? t(R.string.online) : t(R.string.offline)));
        wrap.addView(statRow("clock", t(R.string.last_login), niceDate(r.lastAccess), timeAgoText(r.lastAccess)));
        wrap.addView(statRow("calendar", t(R.string.creation), niceDateOnly(r.memberSince), timeAgoText(r.memberSince)));
        // Amigos, quartos, grupos, fotos e emblemas já possuem seções completas
        // abaixo; repetir os mesmos números aqui criava cinco cards redundantes.
        wrap.addView(statRow("star", t(R.string.stars), formatNumericText(emptyDash(r.starGems))));
        wrap.addView(statRow("level", t(R.string.level), formatNumericText(emptyDash(r.level))));
    }

    private LinearLayout statRow(String icon, String label, String value) {
        return statRow(icon, label, value, "");
    }

    private LinearLayout statRow(String icon, String label, String value, String tooltip) {
        LinearLayout row = card(dp(18));
        applyProfilePrivateBorder(row, dp(18));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(7), dp(10), dp(7));
        LinearLayout.LayoutParams rp = lp(-1, dp(54), 0, 0, 0, 7);
        row.setLayoutParams(rp);
        if ("status".equals(icon) || "status_online".equals(icon) || "status_offline".equals(icon)) {
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            row.addView(iv, new LinearLayout.LayoutParams(dp(20), dp(20)));
            boolean onlineStatusIcon = "status_online".equals(icon) || (value != null && value.trim().equalsIgnoreCase(t(R.string.online)));
            Glide.with(this).asGif().load(onlineStatusIcon ? R.drawable.online : R.drawable.offline).into(iv);
        } else {
            IconView iv = new IconView(this, icon);
            row.addView(iv, new LinearLayout.LayoutParams(dp(18), dp(18)));
        }
        LinearLayout texts = new LinearLayout(this); texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1); tp.leftMargin = dp(9); row.addView(texts, tp);
        texts.addView(text(label, 11, Color.argb(190,255,255,255), false));
        texts.addView(text(value == null || value.isEmpty() || "null".equalsIgnoreCase(value) ? "" : value, 14, Color.WHITE, true));
        if (tooltip != null && !tooltip.trim().isEmpty() && !"—".equals(tooltip.trim())) {
            row.setOnClickListener(v -> toast(tooltip));
        }
        return row;
    }

    private void addSelectedBadges(ArrayList<JSONObject> list) {
        if (list.isEmpty()) return;
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setFillViewport(true);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setMinimumWidth(getResources().getDisplayMetrics().widthPixels - dp(36));
        row.setPadding(dp(2), dp(2), dp(2), dp(2));
        hsv.addView(row);
        resultWrap.addView(hsv, lp(-1, dp(72), 0, 0, 0, 14));
        for (int i = 0; i < Math.min(list.size(), 12); i++) {
            JSONObject b = list.get(i);
            String code = firstText(b, "code", "badgeCode");

            FrameLayout cell = new FrameLayout(this);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(54), dp(58));
            p.rightMargin = dp(8);
            row.addView(cell, p);

            ImageView img = new ImageView(this);
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setPadding(dp(2), dp(2), dp(2), dp(2));
            cell.addView(img, new FrameLayout.LayoutParams(dp(50), dp(50), Gravity.CENTER));
            if (!code.isEmpty()) loadImage(img, badgeImageUrl(code));

            if (isTodayCreationTime(badgeObtainedDate(b))) {
                TextView newBadge = text(newBadgeLabel(), 8, Color.WHITE, true);
                newBadge.setGravity(Gravity.CENTER);
                newBadge.setPadding(dp(5), 0, dp(5), 0);
                newBadge.setBackground(round(Color.rgb(39, 174, 96), dp(999), Color.argb(95,255,255,255), 1));
                FrameLayout.LayoutParams nlp = new FrameLayout.LayoutParams(-2, dp(16), Gravity.TOP | Gravity.RIGHT);
                nlp.topMargin = dp(1);
                nlp.rightMargin = dp(1);
                cell.addView(newBadge, nlp);
            }
            final JSONObject badgeObj = b;
            cell.setOnClickListener(v -> showBadgeDialog(badgeObj));
        }
    }

    private void addPreviousNames(ArrayList<JSONObject> list) {
        if (list.isEmpty()) return;
        LinearLayout c = sectionCard(t(R.string.previous_names), list.size(), true);
        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(true);
        sv.setScrollbarFadingEnabled(false);
        tintScrollBar(sv);
        sv.setOnTouchListener((view, event) -> {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        LinearLayout inner = new LinearLayout(this); inner.setOrientation(LinearLayout.VERTICAL);
        sv.addView(inner, new ScrollView.LayoutParams(-1, -2));
        c.addView(sv, lp(-1, dp(Math.min(220, Math.max(64, 68 * Math.min(list.size(), 4)))), 0, 0, 0, 0));
        for (int i=0; i<Math.min(list.size(), 40); i++) {
            JSONObject o = list.get(i);
            String n = firstText(o, "name", "oldName", "username");
            String d = firstText(o, "changedAt", "date", "timestamp", "createdAt");
            inner.addView(historyItem(n.isEmpty() ? t(R.string.previous_name_fallback) : n, niceDate(d)));
        }
    }

    private void addPreviousMottos(ProfileResult profileResult, ArrayList<JSONObject> list) {
        if (list == null || list.isEmpty()) return;

        ArrayList<JSONObject> valid = new ArrayList<>();
        for (JSONObject item : list) {
            String m = firstText(item, "text", "motto", "mission");
            if (!m.isEmpty()) valid.add(item);
        }
        if (valid.isEmpty()) return;

        LinearLayout c = sectionCard(t(R.string.previous_mottos), valid.size(), true);

        FrameLayout slideHost = new FrameLayout(this);
        slideHost.setClipChildren(false);
        slideHost.setClipToPadding(false);
        c.addView(slideHost, lp(-1, -2, 0, 0, 0, 8));

        HorizontalScrollView dotsScroll = new HorizontalScrollView(this);
        dotsScroll.setHorizontalScrollBarEnabled(false);
        dotsScroll.setFillViewport(valid.size() <= 18);
        LinearLayout dots = new LinearLayout(this);
        dots.setOrientation(LinearLayout.HORIZONTAL);
        dots.setGravity(Gravity.CENTER);
        dots.setPadding(dp(6), dp(2), dp(6), dp(2));
        dotsScroll.addView(dots, new HorizontalScrollView.LayoutParams(-2, dp(24)));
        c.addView(dotsScroll, lp(-1, dp(28), 0, 0, 0, 0));

        int savedIndex = profileResult == null ? 0 : profileResult.previousMottosSlideIndex;
        final int[] index = {Math.max(0, Math.min(savedIndex, valid.size() - 1))};
        final int[] animationDirection = {0};
        final float[] downX = {0f};
        final float[] downY = {0f};
        final boolean[] horizontalGesture = {false};
        Runnable[] render = new Runnable[1];

        render[0] = () -> {
            if (index[0] < 0) index[0] = 0;
            if (index[0] >= valid.size()) index[0] = valid.size() - 1;
            JSONObject item = valid.get(index[0]);
            String mission = firstText(item, "text", "motto", "mission");
            String date = niceDate(firstText(item, "changedAt", "date", "timestamp", "createdAt"));

            slideHost.removeAllViews();
            LinearLayout slide = missionQuoteSlide(mission, date);
            float enter = animationDirection[0] == 0 ? 0f : dp(26) * animationDirection[0];
            slide.setAlpha(animationDirection[0] == 0 ? 1f : 0f);
            slide.setTranslationX(enter);
            slideHost.addView(slide, new FrameLayout.LayoutParams(-1, -2));
            if (animationDirection[0] != 0) {
                slide.animate().alpha(1f).translationX(0f).setDuration(180L).start();
            }

            dots.removeAllViews();
            for (int i = 0; i < valid.size(); i++) {
                final int dotIndex = i;
                View dot = new View(this);
                boolean active = i == index[0];
                dot.setBackground(round(
                        active ? purple : Color.argb(lightTheme ? 75 : 105, 139, 52, 217),
                        dp(999),
                        Color.TRANSPARENT,
                        0
                ));
                int dotSize = active ? dp(9) : dp(7);
                LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(dotSize, dotSize);
                dlp.leftMargin = dp(3);
                dlp.rightMargin = dp(3);
                dot.setLayoutParams(dlp);
                dot.setOnClickListener(v -> {
                    if (dotIndex == index[0]) return;
                    animationDirection[0] = dotIndex > index[0] ? 1 : -1;
                    index[0] = dotIndex;
                    syncPreviousMottoSlideIndex(profileResult, index[0]);
                    render[0].run();
                });
                dots.addView(dot);
            }
            syncPreviousMottoSlideIndex(profileResult, index[0]);
            if (valid.size() > 18) {
                dotsScroll.post(() -> {
                    View activeDot = index[0] < dots.getChildCount()
                            ? dots.getChildAt(index[0]) : null;
                    if (activeDot != null) {
                        int target = Math.max(0,
                                activeDot.getLeft() - (dotsScroll.getWidth() - activeDot.getWidth()) / 2);
                        dotsScroll.smoothScrollTo(target, 0);
                    }
                });
            }
            animationDirection[0] = 0;
        };

        slideHost.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX[0] = event.getX();
                    downY[0] = event.getY();
                    horizontalGesture[0] = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getX() - downX[0];
                    float dy = event.getY() - downY[0];
                    if (!horizontalGesture[0] && Math.abs(dx) > dp(10)
                            && Math.abs(dx) > Math.abs(dy)) {
                        horizontalGesture[0] = true;
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float totalDx = event.getX() - downX[0];
                    float totalDy = event.getY() - downY[0];
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    if (event.getActionMasked() == MotionEvent.ACTION_UP
                            && Math.abs(totalDx) >= dp(38)
                            && Math.abs(totalDx) > Math.abs(totalDy) * 1.15f) {
                        if (totalDx < 0 && index[0] < valid.size() - 1) {
                            animationDirection[0] = 1;
                            index[0]++;
                            render[0].run();
                        } else if (totalDx > 0 && index[0] > 0) {
                            animationDirection[0] = -1;
                            index[0]--;
                            render[0].run();
                        }
                    }
                    horizontalGesture[0] = false;
                    return true;
                default:
                    return true;
            }
        });

        render[0].run();
    }

    private LinearLayout missionQuoteSlide(String mission, String date) {
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.HORIZONTAL);
        outer.setGravity(Gravity.TOP);
        outer.setPadding(dp(12), dp(10), dp(12), dp(10));
        outer.setBackground(round(
                lightTheme ? Color.rgb(250, 250, 252) : Color.argb(20, 255, 255, 255),
                dp(18),
                lightTheme ? Color.rgb(222, 222, 228) : Color.argb(30, 255, 255, 255),
                1
        ));

        View quoteBar = new View(this);
        quoteBar.setBackground(round(purple, dp(999), Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(dp(4), -1);
        barLp.rightMargin = dp(12);
        outer.addView(quoteBar, barLp);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setGravity(Gravity.TOP);
        outer.addView(body, new LinearLayout.LayoutParams(0, -2, 1));

        TextView quote = text("“", 27, purple, true);
        quote.setGravity(Gravity.LEFT);
        quote.setIncludeFontPadding(false);
        body.addView(quote, lp(-1, dp(24), 0, 0, 0, 0));

        TextView missionText = habboText(mission == null ? "" : mission, 16, true);
        missionText.setTextColor(lightTheme ? Color.rgb(32, 32, 36) : Color.WHITE);
        missionText.setGravity(Gravity.LEFT);
        missionText.setLineSpacing(dp(2), 1f);
        // Altura totalmente baseada no texto, sem espaço vertical fixo.
        body.addView(missionText, new LinearLayout.LayoutParams(-1, -2));

        if (date != null && !date.trim().isEmpty() && !"—".equals(date.trim())) {
            TextView dateText = text(
                    date,
                    12,
                    lightTheme ? Color.rgb(105, 105, 112) : Color.argb(190, 255, 255, 255),
                    false
            );
            dateText.setGravity(Gravity.LEFT);
            body.addView(dateText, lp(-1, -2, 0, 5, 0, 0));
        }
        return outer;
    }

    private void syncPreviousMottoSlideIndex(ProfileResult rendered, int index) {
        if (rendered != null) rendered.previousMottosSlideIndex = Math.max(0, index);
        ProfileResult source = activeProfileSource;
        if (source != null && rendered != null && sameProfile(source, rendered)
                && normalizeHotelKey(source.hotelKey).equals(normalizeHotelKey(rendered.hotelKey))) {
            synchronized (source) {
                source.previousMottosSlideIndex = Math.max(0, index);
            }
        }
    }

    private LinearLayout historyItem(String main, String date) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(12), dp(10), dp(12), dp(10));
        box.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(22,255,255,255), dp(16), lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255), 1));
        box.setLayoutParams(lp(-1, -2, 0, 0, 0, 10));
        TextView title = habboText(main == null ? "" : main, 16, true);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
        title.setLineSpacing(dp(2), 1f);
        box.addView(title, lp(-1, -2, 0, 0, 0, 4));
        if (date != null && !date.isEmpty() && !date.equals("—")) {
            TextView d = text(date, 12, Color.argb(185,255,255,255), false);
            d.setGravity(Gravity.CENTER);
            box.addView(d, lp(-1, -2, 0, 0, 0, 0));
        }
        return box;
    }

    private TextView mottoItem(String main, String date) {
        TextView v = habboText(main + (date == null || date.isEmpty() || date.equals("—") ? "" : "\n" + date), 16, true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(12), dp(12), dp(12), dp(12));
        v.setLineSpacing(dp(4), 1f);
        v.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
        v.setBackground(round(lightTheme ? Color.rgb(245,245,245) : Color.argb(22,255,255,255), dp(16), Color.argb(24,255,255,255), 1));
        v.setLayoutParams(lp(-1, -2, 0, 0, 0, 10));
        return v;
    }

    private void addPreviousStyles(ProfileResult profileResult) {
        if (profileResult == null) return;
        ArrayList<JSONObject> list = profileResult.previousStyles;
        if (list.isEmpty() && !profileResult.stylesHasMore && !profileResult.stylesLoading) return;
        final int loaded = list.size();
        final int totalLabel = Math.max(profileResult.stylesTotal, loaded);
        LinearLayout c = sectionCardWithLoadMore(t(R.string.previous_styles), loaded, totalLabel > 0 ? totalLabel : loaded, profileResult.stylesHasMore || profileResult.stylesLoading, profileResult.stylesLoading, () -> loadMoreStyles(profileResult, null));
        HorizontalScrollView hsv = new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); hsv.addView(row);
        c.addView(hsv, lp(-1, dp(172), 0, 0, 0, 8));
        final HorizontalScrollView stylesHsv = hsv;
        if (stylesScrollX > 0) stylesHsv.post(() -> stylesHsv.scrollTo(stylesScrollX, 0));
        for (int i=0; i<loaded; i++) {
            JSONObject o = list.get(i);
            String fig = firstText(o, "figureString", "figure", "look");
            if (fig.isEmpty()) continue;
            LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setPadding(dp(8),dp(8),dp(8),dp(8)); box.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(18), lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255),1));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(dp(106), dp(162)); bp.rightMargin = dp(12); row.addView(box, bp);
            ImageView img = new ImageView(this); img.setScaleType(ImageView.ScaleType.FIT_CENTER); box.addView(img, new LinearLayout.LayoutParams(-1, dp(112)));
            loadImage(img, avatarSmall(fig));
            TextView dt = text(niceDate(firstText(o, "changedAt", "date", "createdAt", "creationTime")), 12, Color.argb(185,255,255,255), false); dt.setGravity(Gravity.CENTER); dt.setMaxLines(2); box.addView(dt, lp(-1,-2,0,4,0,0));
            final String finalFig = fig;
            box.setOnClickListener(v -> showClothesDialog(finalFig, niceDate(firstText(o, "changedAt", "date", "createdAt", "creationTime"))));
        }
        if (profileResult.stylesHasMore && !profileResult.stylesLoading) {
            View more = c.findViewWithTag("load_more_header_button");
            if (more != null) more.setOnClickListener(v -> loadMoreStyles(profileResult, stylesHsv));
        }
        // Keep the slot in the profile hierarchy during progressive renders. Loading is
        // automatically deferred while entitlement is being verified.
        addBannerToResultWrap(buildPreviousStylesBannerAd(), 18);
    }

    private void showClothesDialog(String figure, String date) {
        final Dialog dialog = new Dialog(this);

        LinearLayout rootDialog = new LinearLayout(this);
        rootDialog.setOrientation(LinearLayout.VERTICAL);
        rootDialog.setPadding(dp(18), dp(18), dp(18), dp(18));
        rootDialog.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        dialog.setContentView(rootDialog);
        applySafeAreaInsets(dialog.getWindow(), rootDialog);

        String cleanDate = date == null ? "" : date.trim();
        TextView title = text(t(R.string.looks) + (cleanDate.isEmpty() ? "" : " — " + cleanDate), 18, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        rootDialog.addView(title, lp(-1,-2,0,0,0,12));

        View line = new View(this);
        line.setBackgroundColor(lightTheme ? Color.rgb(220,220,220) : Color.argb(35,255,255,255));
        rootDialog.addView(line, lp(-1,1,6,0,6,12));

        final ScrollView clothesScroll = new ScrollView(this);
        clothesScroll.setVerticalScrollBarEnabled(true);
        clothesScroll.setScrollbarFadingEnabled(false);
        tintScrollBar(clothesScroll);
        clothesScroll.setOnTouchListener((view, event) -> {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        final LinearLayout clothesContainer = new LinearLayout(this);
        clothesContainer.setOrientation(LinearLayout.VERTICAL);
        clothesScroll.addView(clothesContainer, new ScrollView.LayoutParams(-1, -2));
        rootDialog.addView(clothesScroll, lp(-1, dp(390), 0, 0, 0, 14));

        LinearLayout loadingBox = new LinearLayout(this);
        loadingBox.setOrientation(LinearLayout.HORIZONTAL);
        loadingBox.setGravity(Gravity.CENTER);
        ProgressBar clothesSpinner = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        if (Build.VERSION.SDK_INT >= 21) clothesSpinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(purple));
        loadingBox.addView(clothesSpinner, new LinearLayout.LayoutParams(dp(28), dp(28)));
        TextView loading = text(t(R.string.loading_clothes), 14, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, false);
        LinearLayout.LayoutParams ltp = new LinearLayout.LayoutParams(-2, -2);
        ltp.leftMargin = dp(10);
        loadingBox.addView(loading, ltp);
        clothesContainer.addView(loadingBox, lp(-1,-2,0,18,0,18));

        Button close = new Button(this);
        close.setText(t(R.string.close));
        close.setAllCaps(false);
        close.setTextColor(Color.WHITE);
        close.setBackground(grad(dp(14), purple2, purple));
        rootDialog.addView(close, lp(-1, dp(48), 0, 0, 0, 0));
        close.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(params);
        }

        executor.execute(() -> {
            try {
                JSONObject data = unwrap(getJson(habbodexFigureUrl(figure)));
                final ArrayList<JSONObject> clothes = normalizeClothingEntries(data);
                enrichClothingEntriesWithHabbonews(clothes, figure);
                final ArrayList<JSONObject> visibleClothes = new ArrayList<>();
                for (JSONObject item : clothes) {
                    if (item != null && !isDefaultClothing(item)) visibleClothes.add(item);
                }
                runOnUiThread(() -> {
                    clothesContainer.removeAllViews();
                    if (visibleClothes.isEmpty()) {
                        clothesContainer.addView(mottoItem(t(R.string.no_clothes_found), ""));
                        return;
                    }
                    for (int i=0; i<visibleClothes.size(); i++) {
                        clothesContainer.addView(clothingRow(visibleClothes.get(i)));
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> loading.setText(t(R.string.cannot_load_clothes)));
            }
        });
    }

    private LinearLayout clothingRow(JSONObject o) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),dp(10),dp(12),dp(10)); row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(26,255,255,255), dp(14), lightTheme ? Color.rgb(220,220,220) : Color.argb(28,255,255,255),1));
        row.setLayoutParams(lp(-1, -2, 0, 0, 0, 10));
        ImageView img = new ImageView(this); img.setScaleType(ImageView.ScaleType.FIT_CENTER); row.addView(img, new LinearLayout.LayoutParams(dp(40), dp(40)));
        String code = firstText(o, "code", "classname", "className", "id");
        if (setClothingRarityIcon(img, o)) img.setContentDescription(t(R.string.rarity));
        LinearLayout txt = new LinearLayout(this); txt.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1); tp.leftMargin = dp(12); row.addView(txt,tp);
        String name = clothingName(o, code);
        TextView nm = habboText(name.isEmpty()?t(R.string.item):name, 15, true); nm.setMaxLines(2); nm.setEllipsize(TextUtils.TruncateAt.END); txt.addView(nm);
        String lineCode = clothingLineName(o, code);
        txt.addView(text(lineCode.isEmpty()?code:lineCode, 13, muted, false));
        return row;
    }


    private boolean hasCompleteClothingName(JSONObject item) {
        return item != null
                && !isDefaultClothing(item)
                && !completeClothingName(item).isEmpty();
    }

    private String completeClothingName(JSONObject item) {
        if (item == null) return "";
        String code = firstText(item, "code", "classname", "className", "id");
        String categoryIdentity = normalizeClothingIdentity(clothingLineName(item, ""));
        ArrayList<String> candidates = new ArrayList<>();
        JSONObject localeNames = item.optJSONObject("localeNames");
        if (localeNames != null) {
            addClothingNameCandidate(candidates, pickLocalizedValue(localeNames, ""));
            Iterator<String> localeKeys = localeNames.keys();
            while (localeKeys.hasNext()) {
                addClothingNameCandidate(candidates, localeNames.optString(localeKeys.next(), ""));
            }
        }
        addClothingNameCandidate(candidates, firstText(item, "name"));
        addClothingNameCandidate(candidates, firstText(item, "publicName"));
        addClothingNameCandidate(candidates, firstText(item, "furniName"));

        for (String candidate : candidates) {
            String name = sanitizeClothingLabel(candidate);
            if (name.isEmpty()) continue;
            if (!code.isEmpty()
                    && normalizeClothingIdentity(name).equals(normalizeClothingIdentity(code))) continue;
            if (!categoryIdentity.isEmpty()
                    && normalizeClothingIdentity(name).equals(categoryIdentity)) continue;
            if (!looksLikeClothingCode(name)) return name;
        }
        return "";
    }

    private void addClothingNameCandidate(ArrayList<String> candidates, String value) {
        if (value == null) return;
        String clean = value.trim();
        if (clean.isEmpty() || "null".equalsIgnoreCase(clean) || candidates.contains(clean)) return;
        candidates.add(clean);
    }

    private String normalizeClothingIdentity(String value) {
        return (value == null ? "" : value)
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    private boolean looksLikeClothingCode(String value) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) return true;
        String plain = java.text.Normalizer.normalize(clean, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        String technical = plain.replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        String slots = "(?:hd|hr|ch|lg|sh|ha|he|ea|fa|cp|ca|cc|wa|pt|mc)";
        if (technical.matches("^\\d+$")) return true;
        // Também elimina rótulos produzidos pela fonte como "ch-240 de camisas".
        if (technical.matches("^" + slots + "_?\\d+(?:_\\d+)*(?:_[a-z0-9]+)*$")) return true;
        if (technical.matches("^(?:nft|kld)_?\\d+(?:_\\d+)*(?:_name)?$")) return true;
        if (technical.matches("^(?:clothing|figure|avatar|look|furni)(?:_[a-z0-9]+)+$")) return true;
        if (isGenericClothingCategory(technical)) return true;

        boolean technicalSeparators = clean.matches(".*[_.:/].*")
                || (!clean.contains(" ") && clean.contains("-"));
        return technicalSeparators && technical.matches(
                "^(?:hair|hairstyle|shirt|top|trousers?|pants?|shoes?|hat|head|face|coat|jacket|accessory|accessories|belt|waist|chest|ear|hand)(?:_[a-z0-9]+)+$"
        );
    }

    private boolean isGenericClothingCategory(String technical) {
        if (technical == null || technical.isEmpty()) return false;
        return technical.matches(
                "^(?:rosto_corpo|face_body|cabelo|hair|camisas?|shirts?|calcas?|trousers?|pants?|sapatos?|shoes?|chapeus?|hats?|acessorios_de_cabeca|head_accessories|acessorios_faciais|face_accessories|estampas|prints|casacos|coats|acessorios_de_peito|chest_accessories|acessorios_de_orelha|ear_accessories|acessorios_de_mao|hand_accessories|cintura|waist)$"
        );
    }

    private boolean isDefaultClothing(JSONObject item) {
        if (item == null) return false;
        if (optBoolAny(item, false, "isDefaultClothing", "defaultClothing", "hidden")) {
            return true;
        }
        JSONObject classification = habbonewsRarityRecord(clothingFigureCode(item));
        return classification != null && classification.optInt("h", 0) == 1;
    }

    private String clothingRarityIconCode(JSONObject item) {
        if (item == null) return "";
        String code = firstText(
                item,
                "rarityIconCode", "rarityCode", "habbonewsIconCode", "rarityKey"
        ).trim();
        if (!code.matches("^[A-Za-z0-9]{5,12}$") || HABBONEWS_TRANSPARENT_ICON.equalsIgnoreCase(code)) {
            JSONObject classification = habbonewsRarityRecord(clothingFigureCode(item));
            code = classification == null ? "" : classification.optString("i", "").trim();
        }
        if (!code.matches("^[A-Za-z0-9]{5,12}$") || HABBONEWS_TRANSPARENT_ICON.equalsIgnoreCase(code)) {
            return "";
        }
        return code;
    }

    private int clothingRarityDrawable(String iconCode) {
        String code = iconCode == null ? "" : iconCode.toLowerCase(Locale.ROOT);
        switch (code) {
            case "7alj14m": return R.drawable.rarity_7alj14m;
            case "xfwx5cf": return R.drawable.rarity_xfwx5cf;
            case "3cn4kmp": return R.drawable.rarity_3cn4kmp;
            case "auw3lqg": return R.drawable.rarity_auw3lqg;
            case "fsbsabs": return R.drawable.rarity_fsbsabs;
            case "i9zueld": return R.drawable.rarity_i9zueld;
            case "jiebdv2": return R.drawable.rarity_jiebdv2;
            case "dx0vak3": return R.drawable.rarity_dx0vak3;
            case "kwobfca": return R.drawable.rarity_kwobfca;
            case "tk0irr5": return R.drawable.rarity_tk0irr5;
            default: return 0;
        }
    }

    private boolean setClothingRarityIcon(ImageView view, JSONObject item) {
        if (view == null) return false;
        String iconCode = clothingRarityIconCode(item);
        if (iconCode.isEmpty()) {
            view.setImageDrawable(null);
            view.setVisibility(View.INVISIBLE);
            return false;
        }

        view.clearColorFilter();
        view.setAlpha(1f);
        view.setVisibility(View.VISIBLE);
        int drawableId = clothingRarityDrawable(iconCode);
        if (drawableId != 0) {
            view.setImageResource(drawableId);
            Drawable drawable = view.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                ((BitmapDrawable) drawable).setFilterBitmap(false);
                ((BitmapDrawable) drawable).setDither(false);
            }
            return true;
        }

        // Suporte futuro: se o iframe passar a usar um hash novo, a API envia
        // também a URL exata; os dez hashes atuais continuam locais no APK.
        String remote = firstText(item, "rarityIconUrl", "iconUrl", "imageUrl");
        if (remote.startsWith("https://")) {
            Glide.with(this)
                    .load(remote)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .dontAnimate()
                    .into(view);
            return true;
        }
        view.setImageDrawable(null);
        view.setVisibility(View.INVISIBLE);
        return false;
    }

    private String clothingName(JSONObject o, String fallback) {
        String n = completeClothingName(o);
        return n.isEmpty() ? fallback : n;
    }

    private String sanitizeClothingLabel(String value) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) return "";
        clean = clean.replaceAll("(?iu)\\s*[-–|]\\s*Habbo\\s+(?:Guarda[- ]Roupa|Closet).*?$", "");
        return clean.trim();
    }

    private String clothingLineName(JSONObject o, String fallback) {
        String n = "";
        if (o != null) {
            JSONObject line = o.optJSONObject("line");
            if (line != null) n = pickLocalizedValue(line.optJSONObject("localeNames"), "");
        }
        if (n.isEmpty()) n = firstText(o, "lineCode", "category", "_slot");
        return n.isEmpty() ? fallback : n;
    }

    private ArrayList<String> localeCandidateKeys() {
        ArrayList<String> keys = new ArrayList<>();
        String hotel = normalizeHotelKey(currentHotelKey);
        String lang = currentLang();
        addLocaleKey(keys, hotel);
        if ("com".equals(hotel)) addLocaleKey(keys, "us");
        if ("pt".equals(lang)) { addLocaleKey(keys, "br"); addLocaleKey(keys, "pt"); }
        if ("en".equals(lang)) addLocaleKey(keys, "us");
        addLocaleKey(keys, lang);
        addLocaleKey(keys, Locale.getDefault().getLanguage());
        String[] fallback = {"us", "br", "pt", "es", "fr", "de", "it", "nl", "tr", "fi"};
        for (String f : fallback) addLocaleKey(keys, f);
        return keys;
    }

    private void addLocaleKey(ArrayList<String> keys, String value) {
        if (value == null) return;
        String k = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        if (k.isEmpty()) return;
        if ("com".equals(k)) k = "us";
        if (!keys.contains(k)) keys.add(k);
    }

    private String pickLocalizedValue(JSONObject localeMap, String fallback) {
        if (localeMap == null) return fallback == null ? "" : fallback;
        for (String key : localeCandidateKeys()) {
            String v = localeMap.optString(key, "").trim();
            if (!v.isEmpty() && !"null".equalsIgnoreCase(v)) return v;
        }
        Iterator<String> it = localeMap.keys();
        while (it.hasNext()) {
            String v = localeMap.optString(it.next(), "").trim();
            if (!v.isEmpty() && !"null".equalsIgnoreCase(v)) return v;
        }
        return fallback == null ? "" : fallback;
    }

    private String firstNestedText(JSONObject o, String... path) {
        if (o == null || path == null || path.length == 0) return "";
        Object cur = o;
        for (String k : path) {
            if (!(cur instanceof JSONObject)) return "";
            cur = ((JSONObject)cur).opt(k);
            if (cur == null || cur == JSONObject.NULL) return "";
        }
        String s = String.valueOf(cur).trim();
        return "null".equalsIgnoreCase(s) ? "" : s;
    }

    private ArrayList<JSONObject> normalizeClothingEntries(JSONObject clothingData) {
        ArrayList<JSONObject> out = new ArrayList<>();
        if (clothingData == null) return out;
        String[] slots = {"hr","hd","ch","lg","sh","ha","he","fa","cp","ca","cc","ea","mc","pt","wa"};
        for (String slot : slots) {
            JSONObject item = clothingData.optJSONObject(slot);
            if (item == null) continue;
            String code = firstText(item, "code", "classname", "className", "id");
            if (code.isEmpty()) continue;
            try { item.put("_slot", slot); } catch(Exception ignored) {}
            out.add(item);
        }
        if (!out.isEmpty()) return out;
        return extractList(clothingData, null);
    }


    private JSONObject habbonewsRarityItems() {
        JSONObject ready = habbonewsRarityItemsMemory;
        if (ready != null) return ready;
        synchronized (MainActivity.class) {
            ready = habbonewsRarityItemsMemory;
            if (ready != null) return ready;
            try {
                byte[] compressed = android.util.Base64.decode(
                        HABBONEWS_RARITY_SEED_GZIP_BASE64,
                        android.util.Base64.DEFAULT
                );
                ByteArrayOutputStream decoded = new ByteArrayOutputStream(96 * 1024);
                try (java.util.zip.GZIPInputStream gzip = new java.util.zip.GZIPInputStream(
                        new ByteArrayInputStream(compressed)
                )) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = gzip.read(buffer)) >= 0) {
                        if (count > 0) decoded.write(buffer, 0, count);
                    }
                }
                JSONObject root = new JSONObject(decoded.toString("UTF-8"));
                JSONObject items = root.optJSONObject("items");
                if (items != null && items.length() >= 2500) {
                    habbonewsRarityItemsMemory = items;
                    return items;
                }
            } catch(Exception ignored) {}
            habbonewsRarityItemsMemory = new JSONObject();
            return habbonewsRarityItemsMemory;
        }
    }

    private JSONObject habbonewsRarityRecord(String figureCode) {
        String code = normalizeClothingFigureCode(figureCode, "");
        if (code.isEmpty()) return null;
        return habbonewsRarityItems().optJSONObject(code);
    }

    private String normalizeClothingFigureCode(String raw, String slot) {
        String value = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        String cleanSlot = slot == null ? "" : slot.trim().toLowerCase(Locale.ROOT);
        if (value.isEmpty()) return "";
        if (value.matches("^\\d+$") && cleanSlot.matches("^[a-z]{2}$")) {
            return cleanSlot + "-" + value;
        }
        String[] parts = value.split("-");
        if (parts.length >= 2 && parts[0].matches("^[a-z]{2}$") && parts[1].matches("^\\d+$")) {
            return parts[0] + "-" + parts[1];
        }
        return "";
    }

    private String clothingFigureCode(JSONObject item) {
        if (item == null) return "";
        String direct = firstText(item, "_figureCode", "figureCode");
        String normalized = normalizeClothingFigureCode(direct, "");
        if (!normalized.isEmpty()) return normalized;
        String slot = firstText(item, "_slot", "slot", "type", "partType", "category");
        String raw = firstText(
                item,
                "code", "classname", "className", "furniCode", "id", "typeId", "figureId"
        );
        return normalizeClothingFigureCode(raw, slot);
    }

    private HashMap<String, String> figureCodesBySlot(String figure) {
        HashMap<String, String> out = new HashMap<>();
        if (figure == null || figure.trim().isEmpty()) return out;
        for (String part : figure.split("\\.")) {
            String code = normalizeClothingFigureCode(part, "");
            if (code.isEmpty()) continue;
            int dash = code.indexOf('-');
            if (dash > 0) out.put(code.substring(0, dash), code);
        }
        return out;
    }

    private void applyHabbonewsRarity(JSONObject item, String figureCode) {
        if (item == null) return;
        String code = normalizeClothingFigureCode(figureCode, "");
        if (code.isEmpty()) code = clothingFigureCode(item);
        if (code.isEmpty()) return;
        JSONObject classification = habbonewsRarityRecord(code);
        if (classification == null) return;
        boolean hidden = classification.optInt("h", 0) == 1;
        String iconCode = classification.optString("i", "").trim();
        if (HABBONEWS_TRANSPARENT_ICON.equalsIgnoreCase(iconCode)) iconCode = "";
        try {
            item.put("_figureCode", code);
            item.put("rarityKnown", true);
            item.put("raritySource", "habbonews-iframe");
            item.put("isDefaultClothing", hidden);
            if (!hidden && iconCode.matches("^[A-Za-z0-9]{5,12}$")) {
                item.put("rarityKey", iconCode);
                item.put("rarityCode", iconCode);
                item.put("rarityIconCode", iconCode);
                item.put("habbonewsIconCode", iconCode);
                item.put("rarityIconUrl", "https://i.imgur.com/" + iconCode + ".gif");
            }
        } catch(Exception ignored) {}
    }

    private void enrichClothingEntriesWithHabbonews(ArrayList<JSONObject> clothes, String figure) {
        if (clothes == null || clothes.isEmpty()) return;
        HashMap<String, String> codes = figureCodesBySlot(figure);
        for (JSONObject item : clothes) {
            if (item == null) continue;
            String slot = firstText(item, "_slot", "slot", "type", "partType", "category")
                    .trim().toLowerCase(Locale.ROOT);
            String figureCode = codes.get(slot);
            if (figureCode == null || figureCode.isEmpty()) figureCode = clothingFigureCode(item);
            applyHabbonewsRarity(item, figureCode);
        }
    }

    private void enrichPhotoRoomInfo(ProfileResult r) {
        if (r == null || r.photos == null || r.photos.isEmpty()) return;
        HashMap<String, JSONObject> byRoom = new HashMap<>();
        if (r.rooms != null) {
            for (JSONObject room : r.rooms) {
                String id = firstText(room, "id", "roomId", "room_id");
                if (!id.isEmpty()) byRoom.put(id, room);
            }
        }
        // Durante o perfil, só cruza dados que já chegaram. Consultas individuais
        // ao endpoint de quartos são feitas apenas quando a foto é aberta.
        for (JSONObject photo : r.photos) {
            JSONObject room = byRoom.get(getPhotoRoomId(photo));
            if (room != null) enrichPhotoWithRoomInfo(photo, room);
        }
    }

    private void enrichPhotoWithRoomInfo(JSONObject photo, JSONObject roomInfo) {
        if (photo == null || roomInfo == null) return;
        try {
            String roomName = firstText(
                    roomInfo,
                    "name", "roomName", "room_name", "caption", "title"
            );
            String ownerName = extractNameFromUnknown(roomInfo.opt("owner"));
            if (ownerName.isEmpty()) {
                ownerName = firstText(
                        roomInfo,
                        "ownerName", "owner_name", "roomOwner"
                );
            }
            String ownerFigure = extractFigureFromUnknown(roomInfo.opt("owner"));
            if (ownerFigure.isEmpty()) {
                ownerFigure = firstText(
                        roomInfo,
                        "ownerFigureString", "ownerFigure", "owner_figure_string"
                );
            }
            String ownerId = extractUniqueIdFromUnknown(roomInfo.opt("owner"));
            if (ownerId.isEmpty()) {
                ownerId = firstText(
                        roomInfo,
                        "ownerUniqueId", "ownerId", "owner_id"
                );
            }

            if (!roomName.isEmpty() && getPhotoRoomName(photo).isEmpty()) {
                photo.put("room_name", roomName);
            }
            if (!ownerName.isEmpty() && getPhotoRoomOwnerName(photo).isEmpty()) {
                photo.put("roomOwner", ownerName);
            }
            if (!ownerFigure.isEmpty() && getPhotoRoomOwnerFigure(photo).isEmpty()) {
                photo.put("roomOwnerFigureString", ownerFigure);
            }
            if (!ownerId.isEmpty() && getPhotoRoomOwnerId(photo).isEmpty()) {
                photo.put("roomOwnerId", ownerId);
            }
        } catch(Exception ignored) {}
    }

    private JSONObject fetchRoomInfoById(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) return null;
        try {
            return unwrap(getJson(
                    habboApiUrl("/api/public/rooms/" + enc(roomId.trim()))
            ));
        } catch(Exception ignored) {
            return null;
        }
    }

    private String getPhotoRoomId(JSONObject photo) {
        String rid = firstText(photo, "roomId", "room_id", "roomid");
        if (!rid.isEmpty()) return rid;
        JSONObject room = photo == null ? null : photo.optJSONObject("room");
        if (room != null) rid = firstText(room, "id", "roomId", "room_id");
        return rid;
    }

    private String getPhotoRoomName(JSONObject photo) {
        String room = firstText(photo, "room_name", "roomName", "roomname");
        JSONObject roomObj = photo == null ? null : photo.optJSONObject("room");
        if (room.isEmpty() && roomObj != null) room = firstText(roomObj, "name", "roomName", "caption", "title");
        return room;
    }

    private String getPhotoRoomOwner(JSONObject photo) {
        return getPhotoRoomOwnerName(photo);
    }

    private String getPhotoRoomOwnerName(JSONObject photo) {
        if (photo == null) return "";
        String[] directKeys = {"roomOwner", "roomOwnerName", "ownerName", "owner_name"};
        for (String key : directKeys) {
            Object value = photo.opt(key);
            String name = extractNameFromUnknown(value);
            if (!name.isEmpty()) return name;
        }

        JSONObject roomObj = photo.optJSONObject("room");
        if (roomObj != null) {
            JSONObject owner = roomObj.optJSONObject("owner");
            String name = extractNameFromUnknown(owner);
            if (!name.isEmpty()) return name;

            for (String key : directKeys) {
                name = extractNameFromUnknown(roomObj.opt(key));
                if (!name.isEmpty()) return name;
            }
        }
        return "";
    }

    private String getPhotoRoomOwnerId(JSONObject photo) {
        if (photo == null) return "";
        String[] directKeys = {"roomOwner", "roomOwnerId", "ownerId", "owner", "user", "habbo"};
        for (String key : directKeys) {
            String id = extractUniqueIdFromUnknown(photo.opt(key));
            if (!id.isEmpty()) return id;
        }
        JSONObject roomObj = photo.optJSONObject("room");
        if (roomObj != null) {
            String id = extractUniqueIdFromUnknown(roomObj.opt("owner"));
            if (!id.isEmpty()) return id;
            for (String key : directKeys) {
                id = extractUniqueIdFromUnknown(roomObj.opt(key));
                if (!id.isEmpty()) return id;
            }
        }
        return "";
    }

    private String getPhotoRoomOwnerFigure(JSONObject photo) {
        if (photo == null) return "";
        String[] directKeys = {"roomOwnerFigureString", "ownerFigureString", "ownerFigure", "figureString", "figure"};

        for (String key : directKeys) {
            String figure = extractFigureFromUnknown(photo.opt(key));
            if (!figure.isEmpty()) return figure;
        }

        JSONObject roomObj = photo.optJSONObject("room");
        if (roomObj != null) {
            JSONObject owner = roomObj.optJSONObject("owner");
            String figure = extractFigureFromUnknown(owner);
            if (!figure.isEmpty()) return figure;

            for (String key : directKeys) {
                figure = extractFigureFromUnknown(roomObj.opt(key));
                if (!figure.isEmpty()) return figure;
            }
        }
        return "";
    }

    private String extractNameFromUnknown(Object value) {
        if (value == null || value == JSONObject.NULL) return "";
        if (value instanceof JSONObject) {
            JSONObject o = (JSONObject) value;
            String name = firstText(o, "name", "username", "habboName", "ownerName");
            if (!name.isEmpty()) return name;
            JSONObject owner = o.optJSONObject("owner");
            if (owner != null) return extractNameFromUnknown(owner);
            return "";
        }
        if (value instanceof JSONArray) {
            JSONArray a = (JSONArray) value;
            for (int i = 0; i < a.length(); i++) {
                String name = extractNameFromUnknown(a.opt(i));
                if (!name.isEmpty()) return name;
            }
            return "";
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return "";
        if ((s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"))) {
            try {
                if (s.startsWith("{")) return extractNameFromUnknown(new JSONObject(s));
                return extractNameFromUnknown(new JSONArray(s));
            } catch (Exception ignored) {}
        }
        return s;
    }

    private String extractUniqueIdFromUnknown(Object value) {
        if (value == null || value == JSONObject.NULL) return "";
        if (value instanceof JSONObject) {
            JSONObject o = (JSONObject) value;
            String id = firstText(o, "uniqueId", "id", "habboId", "ownerId", "userId");
            if (!id.isEmpty()) return id;
            JSONObject owner = o.optJSONObject("owner");
            if (owner != null) return extractUniqueIdFromUnknown(owner);
            return "";
        }
        if (value instanceof JSONArray) {
            JSONArray a = (JSONArray) value;
            for (int i = 0; i < a.length(); i++) {
                String id = extractUniqueIdFromUnknown(a.opt(i));
                if (!id.isEmpty()) return id;
            }
            return "";
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return "";
        if ((s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"))) {
            try {
                if (s.startsWith("{")) return extractUniqueIdFromUnknown(new JSONObject(s));
                return extractUniqueIdFromUnknown(new JSONArray(s));
            } catch(Exception ignored) {}
        }
        return "";
    }

    private String extractFigureFromUnknown(Object value) {
        if (value == null || value == JSONObject.NULL) return "";
        if (value instanceof JSONObject) {
            JSONObject o = (JSONObject) value;
            String figure = firstText(o, "figureString", "figure_string", "figure", "avatarFigureString", "ownerFigureString");
            if (!figure.isEmpty()) return figure;
            JSONObject owner = o.optJSONObject("owner");
            if (owner != null) return extractFigureFromUnknown(owner);
            return "";
        }
        if (value instanceof JSONArray) {
            JSONArray a = (JSONArray) value;
            for (int i = 0; i < a.length(); i++) {
                String figure = extractFigureFromUnknown(a.opt(i));
                if (!figure.isEmpty()) return figure;
            }
            return "";
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return "";
        if ((s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"))) {
            try {
                if (s.startsWith("{")) return extractFigureFromUnknown(new JSONObject(s));
                return extractFigureFromUnknown(new JSONArray(s));
            } catch (Exception ignored) {}
        }
        return s.contains("-") ? s : "";
    }

    private String getRoomImageUrl(JSONObject room) {
        String url = normalizeUrl(firstText(room, "thumbnailUrl", "url"));
        return url == null ? "" : url.trim();
    }

    private void addPhotos(ProfileResult profileResult) {
        if (profileResult == null) return;
        ArrayList<JSONObject> list = profileResult.photos;
        if (list.isEmpty() && !profileResult.photosHasMore && !profileResult.photosLoading) return;
        final int loaded = list.size();
        final int totalLabel = Math.max(profileResult.photosTotal, loaded);
        LinearLayout c = sectionCardWithLoadMore(t(R.string.user_photos), loaded, totalLabel > 0 ? totalLabel : loaded, profileResult.photosHasMore || profileResult.photosLoading, profileResult.photosLoading, () -> loadMorePhotos(profileResult, null));
        HorizontalScrollView hsv = new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); hsv.addView(row);
        c.addView(hsv, lp(-1, dp(165), 0, 0, 0, 0));
        final HorizontalScrollView photosHsv = hsv;
        if (photosScrollX > 0) photosHsv.post(() -> photosHsv.scrollTo(photosScrollX, 0));
        for (int i=0; i<loaded; i++) {
            JSONObject o = list.get(i);
            String url = getPhotoUrl(o);
            String date = getPhotoTimestamp(o);
            LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(16), lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255), 1));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(dp(160), dp(160)); bp.rightMargin = dp(12); row.addView(box, bp);
            ImageView img = new ImageView(this); img.setScaleType(ImageView.ScaleType.CENTER_CROP); applyRoundedClip(img, dp(14)); box.addView(img, new LinearLayout.LayoutParams(-1, dp(112)));
            TextView dt = text(date, 12, Color.argb(190,255,255,255), false); dt.setGravity(Gravity.CENTER); box.addView(dt, lp(-1,-2,0,8,0,0));
            if (!url.isEmpty()) { loadImage(img, url); final JSONObject photoObj = o; box.setOnClickListener(v -> showPhotoDialog(photoObj)); }
        }
        if (profileResult.photosHasMore && !profileResult.photosLoading) {
            View more = c.findViewWithTag("load_more_header_button");
            if (more != null) more.setOnClickListener(v -> loadMorePhotos(profileResult, photosHsv));
        }
    }

    private TextView loadMoreButton(String label, int shown, int total) {
        TextView more = new TextView(this);
        more.setGravity(Gravity.CENTER);
        more.setTextColor(Color.WHITE);
        more.setPadding(0, 0, 0, 0);
        more.setBackground(new AddButtonDrawable());
        return more;
    }

    private String getPhotoUrl(JSONObject photo) {
        String url = firstText(photo, "previewUrl", "url", "imageUrl", "photoUrl");
        if (url.isEmpty()) url = findImageUrlDeep(photo);
        return normalizeUrl(url);
    }

    private String getPhotoTimestamp(JSONObject photo) {
        return niceDateOnly(firstText(photo, "creationTime", "time", "createdAt", "formatted_time", "formattedTime"));
    }

    private int getPhotoLikesCount(JSONObject photo) {
        return getPhotoLikerNames(photo).size();
    }

    private void showPhotoDialog(JSONObject photo) {
        String url = getPhotoUrl(photo);
        if (url.isEmpty()) return;

        final Dialog dialog = new Dialog(this);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(14), dp(14), dp(14), dp(14));
        wrap.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        dialog.setContentView(wrap);
        applySafeAreaInsets(dialog.getWindow(), wrap);

        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(-1, -2);
        }

        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        applyRoundedClip(img, dp(16));
        wrap.addView(img, lp(-1, dp(260), 0,0,0,12));
        loadImage(img, url);

        ArrayList<String> likers = getPhotoLikerNames(photo);

        LinearLayout infoGrid = new LinearLayout(this);
        infoGrid.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(infoGrid, lp(-1, -2, 0, 0, 0, 12));

        populatePhotoInfoGrid(infoGrid, photo, dialog);

        if (!likers.isEmpty()) {
            TextView likesTitle = habboText(t(R.string.liked_by), 17, true);
            likesTitle.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
            wrap.addView(likesTitle, lp(-1, -2, 0, 0, 0, 8));

            ScrollView likesScroll = new ScrollView(this);
            likesScroll.setVerticalScrollBarEnabled(true);
            likesScroll.setScrollbarFadingEnabled(false);
            tintScrollBar(likesScroll);
            likesScroll.setOnTouchListener((view, event) -> {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });

            LinearLayout likesList = new LinearLayout(this);
            likesList.setOrientation(LinearLayout.VERTICAL);
            likesScroll.addView(likesList, new ScrollView.LayoutParams(-1, -2));
            wrap.addView(likesScroll, lp(-1, dp(Math.min(230, Math.max(82, 54 * Math.min(likers.size(), 4)))), 0, 0, 0, 12));

            for (String liker : likers) {
                likesList.addView(likerRow(liker, dialog));
            }
        }

        Button close = new Button(this);
        close.setText(t(R.string.close));
        close.setAllCaps(false);
        close.setTextColor(Color.WHITE);
        close.setBackground(grad(dp(14), purple2, purple));
        wrap.addView(close, lp(-1, dp(46), 0, 0, 0, 0));
        close.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(shownWindow.getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            shownWindow.setAttributes(params);
        }

        String roomId = getPhotoRoomId(photo);
        boolean needsRoomDetails = !roomId.isEmpty()
                && (getPhotoRoomName(photo).isEmpty() || getPhotoRoomOwnerName(photo).isEmpty())
                && !photo.optBoolean("_officialRoomLookupDone", false);
        if (needsRoomDetails) {
            try { photo.put("_officialRoomLookupDone", true); } catch(Exception ignored) {}
            executor.execute(() -> {
                JSONObject roomInfo = fetchRoomInfoById(roomId);
                if (roomInfo == null) return;
                enrichPhotoWithRoomInfo(photo, roomInfo);
                runOnUiThread(() -> {
                    if (!dialog.isShowing()) return;
                    populatePhotoInfoGrid(infoGrid, photo, dialog);
                });
            });
        }
    }

    private void populatePhotoInfoGrid(
            LinearLayout infoGrid,
            JSONObject photo,
            Dialog dialog
    ) {
        if (infoGrid == null || photo == null) return;
        infoGrid.removeAllViews();
        String room = getPhotoRoomName(photo);
        String ownerName = getPhotoRoomOwnerName(photo);
        String ownerFigure = getPhotoRoomOwnerFigure(photo);
        String ownerId = getPhotoRoomOwnerId(photo);

        infoGrid.addView(photoInfoCard(t(R.string.date), getPhotoTimestamp(photo), "", ""));
        if (!room.isEmpty()) {
            infoGrid.addView(photoInfoCard(t(R.string.room), room, "", ""));
        }
        if (!ownerName.isEmpty()) {
            LinearLayout ownerCard = photoInfoCard(
                    t(R.string.owner),
                    ownerName,
                    ownerFigure,
                    ownerName,
                    ownerId
            );
            ownerCard.setOnClickListener(v -> {
                if (dialog != null) dialog.dismiss();
                openProfileReference(
                        ownerName,
                        ownerId,
                        ownerFigure,
                        currentHotelKey
                );
            });
            infoGrid.addView(ownerCard);
        }
        infoGrid.addView(photoInfoCard(
                t(R.string.likes),
                formatCount(getPhotoLikerNames(photo).size()),
                "",
                ""
        ));
    }

    private LinearLayout photoInfoCard(String label, String value, String figure, String nickToOpen) {
        return photoInfoCard(label, value, figure, nickToOpen, "");
    }

    private LinearLayout photoInfoCard(String label, String value, String figure, String nickToOpen, String uniqueIdToOpen) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(24,255,255,255), dp(15), lightTheme ? Color.rgb(220,220,220) : Color.argb(30,255,255,255), 1));
        row.setLayoutParams(lp(-1, -2, 0, 0, 0, 8));

        boolean hasHead = (figure != null && !figure.isEmpty()) || (nickToOpen != null && !nickToOpen.trim().isEmpty());
        if (hasHead) {
            ImageView head = new ImageView(this);
            head.setScaleType(ImageView.ScaleType.FIT_CENTER);
            row.addView(head, new LinearLayout.LayoutParams(dp(42), dp(42)));
            loadHeadImageForKnownProfile(head, figure, uniqueIdToOpen, nickToOpen, currentHotelKey);
        }

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        if (hasHead) tp.leftMargin = dp(10);
        row.addView(texts, tp);

        TextView lb = text(label, 12, Color.argb(185,255,255,255), false);
        texts.addView(lb);
        TextView val = habboText(value == null || value.isEmpty() ? "" : value, 15, true);
        val.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
        val.setMaxLines(2);
        val.setEllipsize(TextUtils.TruncateAt.END);
        texts.addView(val);

        if (nickToOpen != null && !nickToOpen.trim().isEmpty()) {
            final String nick = nickToOpen.trim();
            row.setOnClickListener(v -> {
                openProfileReference(nick, uniqueIdToOpen, figure, currentHotelKey);
            });
        }
        return row;
    }

    private LinearLayout likerRow(String nick, Dialog dialogToClose) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(7), dp(10), dp(7));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(20,255,255,255), dp(14), lightTheme ? Color.rgb(220,220,220) : Color.argb(25,255,255,255), 1));
        row.setLayoutParams(lp(-1, -2, 0, 0, 0, 7));

        ImageView head = new ImageView(this);
        head.setScaleType(ImageView.ScaleType.FIT_CENTER);
        row.addView(head, new LinearLayout.LayoutParams(dp(42), dp(42)));
        loadHeadImage(head, avatarHeadByName(nick));

        TextView name = habboText(nick, 15, true);
        name.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
        name.setMaxLines(1);
        name.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0, -2, 1);
        np.leftMargin = dp(10);
        row.addView(name, np);

        row.setOnClickListener(v -> {
            if (dialogToClose != null) dialogToClose.dismiss();
            setSearchTextProgrammatically(nick);
            search();
        });

        return row;
    }

    private ArrayList<String> getPhotoLikerNames(JSONObject photo) {
        ArrayList<String> names = new ArrayList<>();
        if (photo == null) return names;

        Object raw = photo.opt("likerNames");
        addLikerNamesFromUnknown(names, raw);

        if (names.isEmpty()) addLikerNamesFromUnknown(names, photo.opt("likes"));
        if (names.isEmpty()) addLikerNamesFromUnknown(names, photo.opt("likers"));

        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String n : names) {
            String clean = n == null ? "" : n.trim();
            if (!clean.isEmpty() && !"null".equalsIgnoreCase(clean)) unique.add(clean);
        }
        return new ArrayList<>(unique);
    }

    private void addLikerNamesFromUnknown(ArrayList<String> out, Object raw) {
        if (out == null || raw == null || raw == JSONObject.NULL) return;

        if (raw instanceof JSONArray) {
            JSONArray a = (JSONArray) raw;
            for (int i = 0; i < a.length(); i++) addLikerNamesFromUnknown(out, a.opt(i));
            return;
        }

        if (raw instanceof JSONObject) {
            String name = extractNameFromUnknown(raw);
            if (!name.isEmpty()) out.add(name);
            return;
        }

        String s = String.valueOf(raw).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return;

        if (s.startsWith("[") || s.startsWith("{")) {
            try {
                if (s.startsWith("[")) addLikerNamesFromUnknown(out, new JSONArray(s));
                else addLikerNamesFromUnknown(out, new JSONObject(s));
                return;
            } catch (Exception ignored) {}
        }

        out.add(s);
    }


    private String newBadgeLabel() {
        return t(R.string.new_badge);
    }

    private void loadMoreBadges(ProfileResult r) {
        if (r == null || r.badgesLoading || !r.badgesHasMore
                || r.uniqueId == null || r.uniqueId.trim().isEmpty()) return;
        final int token = activeSearchToken;
        final int nextPage = r.badgesNextPage <= 1 ? 2 : r.badgesNextPage;
        r.badgesLoading = true;
        executor.execute(() -> {
            try {
                PageResult next = fetchCriticalBadgesPage(
                        r.uniqueId, nextPage, 100, false
                );
                if (!isActiveToken(token) || next == null || !next.success) return;
                synchronized (r) {
                    applyBadgesPage(r, next, false);
                    reconcileProfileSources(r);
                    enrichSelectedBadgesWithOwnership(r);
                }
            } finally {
                r.badgesLoading = false;
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    final int scrollY = mainScroll == null ? 0 : mainScroll.getScrollY();
                    renderProfile(r);
                    if (mainScroll != null && scrollY > 0) {
                        mainScroll.post(() -> mainScroll.scrollTo(0, scrollY));
                    }
                });
            }
        });
    }

    private void loadMoreFriends(ProfileResult r) {
        if (r == null || r.friendsLoading || !r.friendsHasMore
                || r.uniqueId == null || r.uniqueId.trim().isEmpty()) return;
        final int token = activeSearchToken;
        final int nextPage = r.friendsNextPage <= 1 ? 2 : r.friendsNextPage;
        r.friendsLoading = true;
        executor.execute(() -> {
            try {
                PageResult next = fetchCriticalFriendsPage(
                        r.uniqueId,
                        nextPage,
                        100,
                        false
                );
                if (!isActiveToken(token)
                        || next == null
                        || !next.success
                        || next.items == null
                        || next.items.isEmpty()
                        || !allFriendsHaveAddedDates(next.items)) return;

                synchronized (r) {
                    ArrayList<JSONObject> officialFriends = r.officialProfile == null
                            ? new ArrayList<>()
                            : extractList(r.officialProfile, "friends");
                    ArrayList<JSONObject> datedPage = mergeListsEnrichingPrimary(
                            new ArrayList<>(next.items),
                            officialFriends,
                            false
                    );
                    r.friends = mergeListsEnrichingPrimary(r.friends, datedPage, true);
                    r.friendsPagedMode = true;
                    r.friendsDatesReady = allFriendsHaveAddedDates(r.friends);
                    if (next.total > 0) r.friendsTotal = Math.max(r.friendsTotal, next.total);
                    if (!officialFriends.isEmpty()) r.friendsTotal = Math.max(r.friendsTotal, officialFriends.size());
                    r.friendsNextPage = next.nextPage;
                    r.friendsHasMore = next.hasMore
                            || (r.friendsTotal > 0 && r.friends.size() < r.friendsTotal);
                    if (r.friendsHasMore && r.friendsNextPage <= nextPage) {
                        r.friendsNextPage = nextPage + 1;
                    }
                    reconcileProfileSources(r);
                    enrichPhotoRoomInfo(r);
                }
            } finally {
                r.friendsLoading = false;
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    renderProfile(r);
                });
            }
        });
    }

    private void loadMoreRemovedFriends(ProfileResult r) {
        if (r == null || r.removedFriendsLoading || !r.removedFriendsHasMore
                || r.uniqueId == null || r.uniqueId.trim().isEmpty()) return;
        final int token = activeSearchToken;
        final int nextPage = r.removedFriendsNextPage <= 1 ? 2 : r.removedFriendsNextPage;
        r.removedFriendsLoading = true;
        executor.execute(() -> {
            try {
                PageResult next = fetchPage(
                        r.uniqueId,
                        "previous-friends",
                        "previousFriends",
                        nextPage,
                        100
                );
                if (!isActiveToken(token) || !next.success) return;
                synchronized (r) {
                    r.oldFriends = mergeLists(r.oldFriends, next.items);
                    if (next.total > 0) r.removedFriendsTotal = Math.max(r.removedFriendsTotal, next.total);
                    r.removedFriendsNextPage = next.nextPage;
                    r.removedFriendsHasMore = next.hasMore
                            || (r.removedFriendsTotal > 0 && r.oldFriends.size() < r.removedFriendsTotal);
                    if (r.removedFriendsHasMore && r.removedFriendsNextPage <= nextPage) {
                        r.removedFriendsNextPage = nextPage + 1;
                    }
                }
            } finally {
                r.removedFriendsLoading = false;
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    renderProfile(r);
                });
            }
        });
    }

    private void addFriendsTabs(ProfileResult profileResult) {
        if (profileResult == null) return;
        ArrayList<JSONObject> friendsList = profileResult.friendsDatesReady
                ? profileResult.friends
                : new ArrayList<>();
        ArrayList<JSONObject> removedList = profileResult.oldFriends == null
                ? new ArrayList<>()
                : profileResult.oldFriends;
        if (friendsList.isEmpty() && removedList.isEmpty()
                && !profileResult.removedFriendsLoading) return;
        LinearLayout c = sectionCard(null, 0, false);
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        c.addView(tabs, lp(-1, dp(58), 0, 0, 0, 14));

        int visibleFriendsTotal = Math.max(profileResult.friendsTotal, friendsList.size());
        TextView btFriends = tabButton(t(R.string.friends) + " (" + formatCount(visibleFriendsTotal) + ")", true);
        TextView btRemoved = trashTabButton(false);

        tabs.addView(btFriends);
        Space tabSpace = new Space(this);
        tabs.addView(tabSpace, new LinearLayout.LayoutParams(0, 1, 1));
        tabs.addView(btRemoved);

        LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); c.addView(content, lp(-1, -2, 0, 0, 0, 0));
        if (!profileResult.friendsDatesReady && !removedList.isEmpty()
                && !profileResult.friendsTabSelectionTouched) {
            profileResult.friendsTabShowingRemoved = true;
        }
        final boolean[] showingRemoved = {profileResult.friendsTabShowingRemoved};
        final int[] page = {Math.max(1, profileResult.friendsTabPage)};
        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            content.removeAllViews();
            btFriends.setBackground(showingRemoved[0] ? tabBg(false) : tabBg(true));
            btFriends.setTextColor(showingRemoved[0] ? tabInactiveTextColor() : Color.WHITE);
            btRemoved.setBackground(new TrashTabDrawable(showingRemoved[0]));
            btRemoved.setText("");
            ArrayList<JSONObject> data = showingRemoved[0]
                    ? profileResult.oldFriends
                    : (profileResult.friendsDatesReady ? profileResult.friends : new ArrayList<>());
            if (!showingRemoved[0] && page[0] == 1) profileFriendTutorialTarget = null;
            int loadedPages = Math.max(1, (int)Math.ceil(data.size() / 10.0));
            if (page[0] > loadedPages) page[0] = loadedPages;
            profileResult.friendsTabPage = page[0];
            profileResult.friendsTabShowingRemoved = showingRemoved[0];
            renderFriendsPage(content, data, page[0], 10, showingRemoved[0]);
            renderPager(content, data.size(), 10, page, render[0], () -> {
                profileResult.friendsTabPage = page[0];
                profileResult.friendsTabShowingRemoved = showingRemoved[0];
                scrollMainToView(c, dp(12));
                if (showingRemoved[0]
                        && profileResult.removedFriendsHasMore
                        && page[0] >= Math.max(1, (int)Math.ceil(profileResult.oldFriends.size() / 10.0))) {
                    loadMoreRemovedFriends(profileResult);
                } else if (!showingRemoved[0]
                        && profileResult.friendsHasMore
                        && page[0] >= Math.max(1, (int)Math.ceil(profileResult.friends.size() / 10.0))) {
                    loadMoreFriends(profileResult);
                }
            });
        };
        btFriends.setOnClickListener(v -> {
            showingRemoved[0] = false;
            page[0] = 1;
            profileResult.friendsTabSelectionTouched = true;
            profileResult.friendsTabShowingRemoved = false;
            profileResult.friendsTabPage = 1;
            render[0].run();
        });
        btRemoved.setOnClickListener(v -> {
            showingRemoved[0] = true;
            page[0] = 1;
            profileResult.friendsTabSelectionTouched = true;
            profileResult.friendsTabShowingRemoved = true;
            profileResult.friendsTabPage = 1;
            render[0].run();
        });
        render[0].run();
        // Keep the slot present during progressive renders; it will load as soon as ads are allowed.
        addBannerToResultWrap(buildFriendsRemovedBannerAd(), 18);
    }

    private int tabInactiveTextColor() { return lightTheme ? Color.rgb(70,70,70) : Color.argb(150,255,255,255); }

    private TextView tabButton(String s, boolean active) {
        TextView v = habboText(s, 16, true); v.setTextColor(active ? Color.WHITE : tabInactiveTextColor()); v.setGravity(Gravity.CENTER); v.setPadding(dp(13),0,dp(13),0); v.setBackground(tabBg(active));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, dp(44)); p.rightMargin = dp(8); v.setLayoutParams(p); return v;
    }

    private TextView trashTabButton(boolean active) {
        TextView v = text("", 16, Color.WHITE, true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(0, 0, 0, 0);
        v.setBackground(new TrashTabDrawable(active));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(46), dp(44));
        p.leftMargin = dp(8);
        v.setLayoutParams(p);
        return v;
    }

    private Drawable tabBg(boolean active) { return active ? grad(dp(13), purple2, purple) : round(lightTheme ? Color.rgb(244,244,246) : Color.rgb(18,17,25), dp(13), lightTheme ? Color.rgb(210,210,214) : Color.rgb(55,50,70), 1); }

    private void renderFriendsPage(LinearLayout content, ArrayList<JSONObject> data, int page, int per, boolean removed) {
        if (data.isEmpty()) { content.addView(centerNote(removed ? t(R.string.no_removed_friend_found) : t(R.string.no_friend_found))); return; }
        int start = Math.max(0, (page-1)*per), end = Math.min(data.size(), start+per);
        for (int i=start; i<end; i+=2) {
            LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); content.addView(row, lp(-1, -2, 0, 0, 0, 12));
            row.addView(friendCard(data.get(i), removed), new LinearLayout.LayoutParams(0, dp(124), 1));
            if (i+1<end) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(124), 1); p.leftMargin = dp(12); row.addView(friendCard(data.get(i+1), removed), p); }
            else { Space sp = new Space(this); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(124), 1); p.leftMargin=dp(12); row.addView(sp,p); }
        }
    }

    private LinearLayout friendCard(JSONObject f, boolean removed) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(8), dp(4), dp(8), dp(8));
        card.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(20,255,255,255), dp(18), (removed || currentProfilePrivate) ? Color.argb(75, 255, 64, 64) : (lightTheme ? Color.rgb(220,220,220) : Color.argb(25,255,255,255)), 1));

        String n = firstText(f, "name", "username", "habboName"); if (n.isEmpty()) n = t(R.string.profile);
        String fig = firstText(f, "figureString", "figure", "look", "avatarFigureString");
        String fid = firstText(f, "uniqueId", "id", "habboId");
        String date = removed
                ? firstText(f, "removedAt", "leftAt", "date", "creationTime", "friendSince", "createdAt")
                : firstText(f, "creationTime", "friendSince", "addedAt", "createdAt", "date");

        FrameLayout headWrap = new FrameLayout(this);
        card.addView(headWrap, new LinearLayout.LayoutParams(-1, dp(64)));

        ImageView head = new ImageView(this);
        head.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(-1, dp(62), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        hp.bottomMargin = dp(-2);
        headWrap.addView(head, hp);
        loadHeadImageForKnownProfile(head, fig, fid, n, currentHotelKey);

        if (isToday(date)) {
            TextView novo = text(newBadgeLabel(), 9, Color.WHITE, true);
            novo.setGravity(Gravity.CENTER);
            novo.setBackground(removed
                ? grad(dp(999), Color.rgb(190, 45, 58), Color.rgb(255, 92, 92))
                : grad(dp(999), Color.rgb(31,184,106), Color.rgb(54,210,127)));
            FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(dp(48), dp(18), Gravity.TOP|Gravity.CENTER_HORIZONTAL);
            headWrap.addView(novo,np);
        }
        if (optBoolAny(f, false, "online", "isOnline")) {
            IconView dot = new IconView(this, "dot");
            FrameLayout.LayoutParams dpv = new FrameLayout.LayoutParams(dp(22), dp(22), Gravity.RIGHT|Gravity.TOP);
            dpv.topMargin=dp(8); dpv.rightMargin=dp(8);
            headWrap.addView(dot, dpv);
        }

        TextView name = habboText(n, 14, true);
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(1);
        name.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(name, lp(-1,-2,0,2,0,6));

        // O HabboDex fornece data e horário tanto para amizades atuais quanto
        // para as removidas; mantém os dois visíveis no card.
        TextView d = text(niceDate(date), 12, Color.argb(185,255,255,255), false);
        d.setGravity(Gravity.CENTER);
        d.setSingleLine(true);
        card.addView(d, lp(-1,-2,0,0,0,0));

        final String fname = n;
        final String friendId = fid;
        bindProfileCardOpenAndHold(
                card,
                fname,
                currentHotelKey,
                fig,
                friendId,
                () -> openProfileReference(fname, friendId, fig, currentHotelKey)
        );
        if (!removed && profileFriendTutorialTarget == null) profileFriendTutorialTarget = card;
        return card;
    }

    private void renderPager(LinearLayout content, int total, int per, int[] page, Runnable rerender) {
        renderPager(content, total, per, page, rerender, null);
    }

    private void renderPager(LinearLayout content, int total, int per, int[] page, Runnable rerender, Runnable pageChanged) {
        int totalPages = Math.max(1, (int)Math.ceil(total/(double)per));
        if (totalPages <= 1) return;
        TextView label = text(tr(R.string.page_of, page[0], totalPages), 16, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, true); label.setGravity(Gravity.CENTER); content.addView(label, lp(-1,-2,0,6,0,12));
        LinearLayout p = new LinearLayout(this); p.setGravity(Gravity.CENTER); p.setOrientation(LinearLayout.HORIZONTAL); content.addView(p, lp(-1, dp(58), 0, 0, 0, 0));
        TextView prev = pageButton("‹", page[0] > 1); p.addView(prev);
        TextView one = pageButton(String.valueOf(page[0]), true); one.setBackground(grad(dp(14), purple2, purple)); p.addView(one);
        TextView next = pageButton("›", page[0] < totalPages); p.addView(next);
        prev.setOnClickListener(v -> {
            if (page[0] > 1) {
                page[0]--;
                rerender.run();
                if (pageChanged != null) pageChanged.run();
            }
        });
        next.setOnClickListener(v -> {
            if (page[0] < totalPages) {
                page[0]++;
                rerender.run();
                if (pageChanged != null) pageChanged.run();
            }
        });
    }

    private TextView pageButton(String s, boolean enabled) { TextView v = text(s, 20, enabled?Color.WHITE:Color.argb(70,255,255,255), true); v.setGravity(Gravity.CENTER); v.setBackground(round(Color.argb(14,255,255,255), dp(14), Color.argb(24,255,255,255), 1)); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(44), dp(44)); p.setMargins(dp(6),0,dp(6),0); v.setLayoutParams(p); return v; }

    private void addRoomsTabs(ArrayList<JSONObject> rooms) {
        if (rooms == null || rooms.isEmpty()) return;
        LinearLayout c = sectionCard(t(R.string.rooms), rooms.size(), true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        c.addView(content, lp(-1, -2, 0, 0, 0, 0));

        final int[] page = {1};
        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            content.removeAllViews();
            renderRoomsPage(content, rooms, page[0], 5, false);
            renderPager(content, rooms.size(), 5, page, render[0], () -> scrollMainToView(c, dp(12)));
        };
        render[0].run();
    }

    private void renderRoomsPage(LinearLayout content, ArrayList<JSONObject> list, int page, int per, boolean oldRoom) {
        if (list.isEmpty()) { content.addView(centerNote(t(R.string.no_rooms_found))); return; }
        int start=(page-1)*per, end=Math.min(list.size(), start+per);
        for (int i=start;i<end;i++) content.addView(roomRow(list.get(i), oldRoom));
    }

    private LinearLayout roomRow(JSONObject room, boolean oldRoom) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(round(
                lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255),
                dp(16),
                (oldRoom || currentProfilePrivate)
                        ? Color.argb(75, 255, 64, 64)
                        : (lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255)),
                1
        ));
        row.setLayoutParams(lp(-1, -2, 0, 0, 0, 12));
        row.setMinimumHeight(dp(122));

        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setBackground(round(
                lightTheme ? Color.rgb(245,245,245) : Color.argb(25,255,255,255),
                dp(12),
                lightTheme ? Color.rgb(220,220,220) : Color.argb(20,255,255,255),
                1
        ));
        applyRoundedClip(img, dp(12));
        row.addView(img, new LinearLayout.LayoutParams(dp(112), dp(88)));
        String image = getRoomImageUrl(room);
        if (!image.isEmpty()) {
            Glide.with(this).load(image).error(R.drawable.quarto).into(img);
        } else {
            img.setImageResource(R.drawable.quarto);
        }

        LinearLayout txt = new LinearLayout(this);
        txt.setOrientation(LinearLayout.VERTICAL);
        txt.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.leftMargin = dp(12);
        row.addView(txt, tp);

        String roomNameValue = firstText(room, "name", "roomName", "caption", "title");
        String shownName = roomNameValue.isEmpty() ? t(R.string.room) : roomNameValue;
        int nameSize = shownName.length() > 70 ? 12 : shownName.length() > 44 ? 13 : shownName.length() > 28 ? 14 : 16;
        TextView roomName = habboText(shownName, nameSize, true);
        roomName.setMaxLines(3);
        roomName.setEllipsize(TextUtils.TruncateAt.END);
        roomName.setLineSpacing(dp(1), 1f);
        txt.addView(roomName, lp(-1, -2, 0, 0, 0, 2));

        String score = emptyDash(firstText(room, "score", "rating"));
        String date = niceDateOnly(firstText(room, "createdAt", "creationTime", "date"));
        String maxVisitors = firstText(
                room,
                "maximumVisitors", "maxVisitors", "maximum_users", "maxUsers",
                "usersMax", "capacity", "visitorLimit", "roomLimit"
        );
        ArrayList<String> roomMetaParts = new ArrayList<>();
        if (!score.isEmpty()) roomMetaParts.add("★ " + score);
        if (!maxVisitors.isEmpty()) roomMetaParts.add("👥 " + formatNumericText(maxVisitors));
        if (!date.isEmpty()) roomMetaParts.add(date);

        String metaText = TextUtils.join("   ", roomMetaParts);
        int metaSize = metaText.length() > 55 ? 11 : 12;
        TextView meta = habboText(metaText, metaSize, false);
        meta.setTextColor(lightTheme
                ? Color.rgb(97,97,97)
                : Color.argb(215,255,255,255));
        meta.setMaxLines(2);
        meta.setLineSpacing(dp(1), 1f);
        txt.addView(meta, lp(-1, -2, 0, 0, 0, 2));

        String desc = firstText(room, "description", "desc");
        if (!desc.isEmpty()) {
            int descSize = desc.length() > 150 ? 10 : desc.length() > 90 ? 11 : desc.length() > 55 ? 12 : 13;
            TextView rd = habboText(desc, descSize, false);
            rd.setTextColor(lightTheme
                    ? Color.rgb(82,82,88)
                    : Color.argb(210,255,255,255));
            rd.setMaxLines(4);
            rd.setEllipsize(TextUtils.TruncateAt.END);
            rd.setLineSpacing(dp(1), 1f);
            txt.addView(rd, lp(-1, -2, 0, 0, 0, 2));
        }

        ArrayList<String> tags = roomTags(room);
        if (!tags.isEmpty()) {
            ArrayList<String> tagParts = new ArrayList<>();
            for (String tag : tags) {
                if (tag == null || tag.trim().isEmpty()) continue;
                tagParts.add("#" + tag.trim());
            }
            String tagsText = TextUtils.join("  ", tagParts);
            if (!tagsText.isEmpty()) {
                int tagsSize = tagsText.length() > 95 ? 10 : 11;
                TextView tagsView = text(
                        tagsText,
                        tagsSize,
                        lightTheme ? Color.rgb(92, 56, 128) : Color.rgb(226, 203, 255),
                        true
                );
                tagsView.setMaxLines(3);
                tagsView.setEllipsize(TextUtils.TruncateAt.END);
                tagsView.setLineSpacing(dp(1), 1f);
                txt.addView(tagsView, lp(-1, -2, 0, 1, 0, 0));
            }
        }
        return row;
    }

    private ArrayList<String> roomTags(JSONObject room) {
        ArrayList<String> out = new ArrayList<>();
        if (room == null) return out;
        Object raw = room.opt("tags");
        if (raw == null || raw == JSONObject.NULL) raw = room.opt("roomTags");
        if (raw == null || raw == JSONObject.NULL) raw = room.opt("tagList");
        collectRoomTags(out, raw);
        return out;
    }

    private void collectRoomTags(ArrayList<String> out, Object raw) {
        if (out == null || raw == null || raw == JSONObject.NULL) return;
        if (raw instanceof JSONArray) {
            JSONArray array = (JSONArray) raw;
            for (int i = 0; i < array.length(); i++) {
                collectRoomTags(out, array.opt(i));
            }
            return;
        }
        if (raw instanceof JSONObject) {
            JSONObject object = (JSONObject) raw;
            String tag = firstText(object, "name", "tag", "value", "text");
            addRoomTag(out, tag);
            return;
        }

        String text = String.valueOf(raw).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) return;
        if (text.startsWith("[") && text.endsWith("]")) {
            try {
                collectRoomTags(out, new JSONArray(text));
                return;
            } catch(Exception ignored) {}
        }
        if (text.contains(",")) {
            for (String part : text.split(",")) addRoomTag(out, part);
        } else {
            addRoomTag(out, text);
        }
    }

    private void addRoomTag(ArrayList<String> out, String raw) {
        if (out == null || raw == null) return;
        String tag = raw.trim();
        while (tag.startsWith("#")) tag = tag.substring(1).trim();
        if (tag.isEmpty() || "null".equalsIgnoreCase(tag)) return;
        for (String existing : out) {
            if (existing.equalsIgnoreCase(tag)) return;
        }
        out.add(tag);
    }

    private void addGroups(ArrayList<JSONObject> list) {
        if (list.isEmpty()) return;
        LinearLayout c = sectionCard(t(R.string.groups), list.size(), true);

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(true);
        sv.setScrollbarFadingEnabled(false);
        tintScrollBar(sv);
        sv.setOnTouchListener((view, event) -> {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        sv.addView(inner, new ScrollView.LayoutParams(-1, -2));
        c.addView(sv, lp(-1, dp(Math.min(430, Math.max(120, 98 * Math.min(list.size(), 4)))), 0, 0, 0, 0));

        for (int i=0; i<list.size(); i++) {
            inner.addView(groupRow(list.get(i)));
        }
    }

    private void addBadgesSection(ProfileResult r) {
        if (r == null) return;
        ArrayList<JSONObject> withAchievements = r.badgesWithAchievements == null
                ? new ArrayList<>() : r.badgesWithAchievements;
        ArrayList<JSONObject> normalSource = !withAchievements.isEmpty()
                ? withAchievements
                : (r.badges == null ? new ArrayList<>() : r.badges);
        // Refiltra no próprio render para garantir que ACH_ nunca apareça quando
        // "Ocultar conquistas" estiver ativo, mesmo após merges progressivos.
        ArrayList<JSONObject> normal = withoutAchievementBadges(normalSource);

        int total = Math.max(0, r.badgesTotal);
        try { total = Math.max(total, Integer.parseInt(String.valueOf(r.totalBadges))); } catch(Exception ignored) {}
        total = Math.max(total, Math.max(normal.size(), withAchievements.size()));
        // Em modo remoto paginado, não mostra uma grade vazia enquanto a
        // primeira página do HabboDex ainda está chegando.
        if (r.badgesPagedMode && normal.isEmpty() && withAchievements.isEmpty()) return;
        if (total <= 0 && normal.isEmpty() && withAchievements.isEmpty()) return;

        LinearLayout c = sectionCard(t(R.string.badges), total, true);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        c.addView(controls, lp(-1, dp(46), 0, 0, 0, 10));

        final boolean[] hideAchievementBadges = {r.hideAchievementBadges};
        final int[] page = {Math.max(1, r.badgesTabPage)};

        TextView hideLabel = text(t(R.string.hide_badges), 15, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, true);
        hideLabel.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(hideLabel, new LinearLayout.LayoutParams(0, dp(42), 1));

        TextView hideToggle = text("", 14, Color.WHITE, true);
        hideToggle.setGravity(Gravity.CENTER);
        hideToggle.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams toggleLp = new LinearLayout.LayoutParams(dp(58), dp(32));
        toggleLp.leftMargin = dp(10);
        controls.addView(hideToggle, toggleLp);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        c.addView(content, lp(-1, -2, 0, 0, 0, 0));

        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            content.removeAllViews();
            hideToggle.setText("");
            hideToggle.setBackground(new AchievementSwitchDrawable(hideAchievementBadges[0]));

            ArrayList<JSONObject> fullData = r.badgesWithAchievements == null
                    ? new ArrayList<>() : r.badgesWithAchievements;
            if (fullData.isEmpty() && r.badges != null) fullData = r.badges;
            ArrayList<JSONObject> data = hideAchievementBadges[0]
                    ? withoutAchievementBadges(fullData)
                    : fullData;
            int loadedPages = Math.max(1, (int)Math.ceil(data.size() / 24.0));
            if (page[0] > loadedPages) page[0] = loadedPages;
            r.badgesTabPage = page[0];
            r.hideAchievementBadges = hideAchievementBadges[0];

            renderBadgePage(content, data, page[0], 24);
            final ArrayList<JSONObject> currentData = data;
            renderPager(content, currentData.size(), 24, page, render[0], () -> {
                r.badgesTabPage = page[0];
                r.hideAchievementBadges = hideAchievementBadges[0];
                if (r.badgesHasMore
                        && page[0] >= Math.max(1, (int)Math.ceil(currentData.size() / 24.0))) {
                    loadMoreBadges(r);
                }
            });
            if (r.badgesHasMore && currentData.size() <= 24 && !r.badgesLoading) {
                uiHandler.post(() -> loadMoreBadges(r));
            }
            if (r.badgesLoading) {
                content.addView(centerNote(t(R.string.loading_history)));
            }
        };

        View.OnClickListener toggleAction = v -> {
            hideAchievementBadges[0] = !hideAchievementBadges[0];
            syncHideAchievementBadgesState(r, hideAchievementBadges[0]);
            page[0] = 1;
            r.badgesTabPage = 1;
            render[0].run();
        };
        hideToggle.setOnClickListener(toggleAction);
        hideLabel.setOnClickListener(toggleAction);

        render[0].run();
    }


    private void syncHideAchievementBadgesState(ProfileResult rendered, boolean hide) {
        if (rendered != null) rendered.hideAchievementBadges = hide;
        ProfileResult source = activeProfileSource;
        if (source != null && rendered != null && sameProfile(source, rendered)
                && normalizeHotelKey(source.hotelKey).equals(normalizeHotelKey(rendered.hotelKey))) {
            synchronized (source) {
                source.hideAchievementBadges = hide;
                source.badgesTabPage = 1;
            }
        }
    }

    private boolean isTodayCreationTime(String raw) {
        return isToday(raw);
    }

    private String badgeObtainedDate(JSONObject badge) {
        return firstText(
                badge,
                "obtainedAt", "acquiredAt", "creationTime", "createdAt", "date"
        );
    }

    private void renderBadgePage(LinearLayout content, ArrayList<JSONObject> list, int page, int per) {
        if (list == null || list.isEmpty()) {
            content.addView(centerNote(t(R.string.no_badges_found)));
            return;
        }

        int start = Math.max(0, (page - 1) * per);
        int end = Math.min(list.size(), start + per);
        int perRow = 4;
        LinearLayout row = null;

        for (int i = start; i < end; i++) {
            if ((i - start) % perRow == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER);
                content.addView(row, lp(-1, dp(60), 0, 0, 0, 8));
            }

            JSONObject badgeObj = list.get(i);
            String code = firstText(badgeObj, "code", "badgeCode");

            FrameLayout cell = new FrameLayout(this);
            cell.setPadding(0, 0, 0, 0);
            cell.setBackgroundColor(Color.TRANSPARENT);
            cell.setClickable(true);

            ImageView img = new ImageView(this);
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setPadding(dp(2), dp(2), dp(2), dp(2));
            cell.addView(img, new FrameLayout.LayoutParams(dp(50), dp(50), Gravity.CENTER));
            if (!code.isEmpty()) loadImage(img, badgeImageUrl(code));

            if (isTodayCreationTime(badgeObtainedDate(badgeObj))) {
                TextView newBadge = text(newBadgeLabel(), 8, Color.WHITE, true);
                newBadge.setGravity(Gravity.CENTER);
                newBadge.setPadding(dp(5), 0, dp(5), 0);
                newBadge.setBackground(round(Color.rgb(39, 174, 96), dp(999), Color.argb(95,255,255,255), 1));
                FrameLayout.LayoutParams nlp = new FrameLayout.LayoutParams(-2, dp(16), Gravity.TOP | Gravity.RIGHT);
                nlp.topMargin = dp(2);
                nlp.rightMargin = dp(2);
                cell.addView(newBadge, nlp);
            }

            cell.setOnClickListener(v -> showBadgeDialog(badgeObj));

            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, dp(54), 1);
            cp.leftMargin = dp(2);
            cp.rightMargin = dp(2);
            if (row != null) row.addView(cell, cp);
        }
    }

    private LinearLayout groupRow(JSONObject g) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),dp(12),dp(12),dp(12)); row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(16), currentProfilePrivate ? Color.argb(75, 255, 64, 64) : (lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255)), 1)); row.setLayoutParams(lp(-1, -2, 0, 0, 0, 12));
        ImageView img = new ImageView(this); img.setScaleType(ImageView.ScaleType.FIT_CENTER); row.addView(img, new LinearLayout.LayoutParams(dp(58), dp(58)));
        String badge = firstText(g,"badgeCode","code"); String badgeUrl = normalizeUrl(firstText(g, "badgeUrl", "imageUrl", "url")); if(!badgeUrl.isEmpty()) loadImage(img, badgeUrl); else if(!badge.isEmpty()) loadImage(img,habboImagingUrl("/habbo-imaging/badge/"+enc(badge)+".gif")); else img.setImageDrawable(new PlaceholderDrawable("groups"));
        LinearLayout txt = new LinearLayout(this); txt.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-2,1); tp.leftMargin=dp(12); row.addView(txt,tp);
        TextView groupName = habboText(firstText(g,"name","groupName").isEmpty() ? t(R.string.group_fallback) : firstText(g,"name","groupName"), 17, true); groupName.setMaxLines(1); groupName.setEllipsize(TextUtils.TruncateAt.END); txt.addView(groupName);
        String desc=firstText(g,"description","desc"); if(!desc.isEmpty()) { TextView gd = habboText(desc, 14, false); gd.setTextColor(Color.argb(220,255,255,255)); gd.setMaxLines(2); gd.setEllipsize(TextUtils.TruncateAt.END); txt.addView(gd); }
        txt.addView(text(niceDate(firstText(g,"createdAt","creationTime","date")), 13, Color.argb(190,255,255,255), false));
        return row;
    }

    private LinearLayout sectionCard(String title, int count, boolean showTitle) {
        LinearLayout c = card(dp(22));
        applyProfilePrivateBorder(c, dp(22));
        c.setPadding(dp(18), dp(18), dp(18), dp(18));
        resultWrap.addView(c, lp(-1, -2, 0, 0, 0, 16));
        if (showTitle && title != null) {
            TextView t = habboText(title + " (" + formatCount(count) + ")", 19, true);
            t.setTextColor(lightTheme ? Color.rgb(81, 48, 133) : Color.rgb(232, 224, 255));
            t.setLetterSpacing(0.015f);
            c.addView(t, lp(-1, -2, 0, 0, 0, 14));
        }
        return c;
    }

    private LinearLayout sectionCardWithLoadMore(String title, int shown, int total, boolean showButton, boolean loading, final Runnable action) {
        LinearLayout c = card(dp(22));
        applyProfilePrivateBorder(c, dp(22));
        c.setPadding(dp(18), dp(18), dp(18), dp(18));
        resultWrap.addView(c, lp(-1, -2, 0, 0, 0, 16));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = habboText(
                title + " (" + formatCount(shown) + "/"
                        + formatCount(Math.max(shown, total)) + ")",
                19,
                true
        );
        t.setTextColor(lightTheme ? Color.rgb(81, 48, 133) : Color.rgb(232, 224, 255));
        t.setLetterSpacing(0.015f);
        header.addView(t, new LinearLayout.LayoutParams(0, -2, 1));

        if (showButton) {
            FrameLayout more = new FrameLayout(this);
            more.setTag("load_more_header_button");
            more.setBackground(new AddButtonDrawable());
            more.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(dp(28), dp(28));
            mp.leftMargin = dp(8);
            header.addView(more, mp);

            if (loading) {
                more.setBackground(grad(dp(7), purple2, purple));
                ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
                if (Build.VERSION.SDK_INT >= 21) pb.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
                FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(dp(14), dp(14), Gravity.CENTER);
                more.addView(pb, pp);
            } else if (action != null) {
                more.setOnClickListener(v -> action.run());
            }
        }

        c.addView(header, lp(-1, dp(38), 0, 0, 0, 12));
        return c;
    }

    private TextView centerNote(String msg) { TextView v = text(msg, 14, muted, false); v.setGravity(Gravity.CENTER); v.setLineSpacing(dp(2),1f); v.setPadding(dp(8), dp(12), dp(8), dp(12)); return v; }



    private void setSuggestionsVisible(boolean visible) {
        if (suggestionsScroll != null) suggestionsScroll.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (suggestionsBox != null) suggestionsBox.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setSuggestionsHeight(int desiredDp) {
        if (suggestionsScroll == null) return;
        ViewGroup.LayoutParams raw = suggestionsScroll.getLayoutParams();
        if (raw != null) {
            raw.height = dp(Math.max(52, Math.min(230, desiredDp)));
            suggestionsScroll.setLayoutParams(raw);
        }
    }

    private void requestDisallowParents(View v, boolean disallow) {
        ViewParent p = v == null ? null : v.getParent();
        while (p != null) {
            p.requestDisallowInterceptTouchEvent(disallow);
            p = p.getParent();
        }
    }

    private void bindNickSuggestions() {
        searchInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!programmaticSearchTextChange && searchInput != null && searchInput.hasFocus()) suppressSuggestions = false;
                scheduleSuggestions(String.valueOf(s));
            }
            public void afterTextChanged(Editable e) {}
        });
    }

    private void scheduleSuggestions(String raw) {
        final String q = raw == null ? "" : raw.trim();
        if (suggestionDebounceTask != null) {
            uiHandler.removeCallbacks(suggestionDebounceTask);
            suggestionDebounceTask = null;
        }
        suggestionRequestId++;
        final int requestId = suggestionRequestId;
        suggestionsBox.removeAllViews();
        setSuggestionsVisible(false);

        if (suppressSuggestions || searchInProgress || searchInput == null || !searchInput.hasFocus()) return;
        if (q.length() < 2) return;

        showSuggestionsLoading();

        suggestionDebounceTask = () -> executor.execute(() -> {
            ArrayList<JSONObject> suggestions = fetchLiveNickSuggestions(q);
            runOnUiThread(() -> {
                if (requestId == suggestionRequestId && !suppressSuggestions && !searchInProgress && searchInput != null && searchInput.hasFocus()) {
                    renderLiveSuggestions(q, suggestions);
                }
            });
        });
        uiHandler.postDelayed(suggestionDebounceTask, 180L);
    }

    private void showSuggestionsLoading() {
        suggestionsBox.removeAllViews();
        setSuggestionsVisible(true);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(round(lightTheme ? Color.rgb(248,248,250) : Color.argb(22,255,255,255), dp(14), lightTheme ? Color.rgb(222,222,226) : Color.argb(30,255,255,255), 1));

        ProgressBar pb = new ProgressBar(this);
        pb.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= 21) pb.setIndeterminateTintList(ColorStateList.valueOf(purple));
        row.addView(pb, new LinearLayout.LayoutParams(dp(28), dp(28)));

        TextView tv = text(t(R.string.loading_suggestions), 13, lightTheme ? Color.rgb(70,70,70) : Color.argb(220,255,255,255), true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.leftMargin = dp(10);
        row.addView(tv, tp);

        suggestionsBox.addView(row, lp(-1, -2, 0, 0, 0, 8));
        setSuggestionsHeight(58);
    }

    private void renderLiveSuggestions(String query, ArrayList<JSONObject> list) {
        suggestionsBox.removeAllViews();
        if (list == null || list.isEmpty()) { setSuggestionsVisible(false); return; }
        setSuggestionsVisible(true);
        TextView title = text(t(R.string.suggestions), 12, Color.argb(210,255,255,255), true);
        suggestionsBox.addView(title, lp(-1, -2, 2, 2, 2, 6));
        int count = Math.min(list.size(), 8);
        for (int i=0; i<count; i++) suggestionsBox.addView(suggestionRow(query, list.get(i), true));
        setSuggestionsHeight(34 + (count * 76));
    }

    private ArrayList<JSONObject> fetchPreviousNickSuggestions(String query) {
        try {
            JSONObject payload = fetchHabbodexSuggestions(query);
            return filterExactPreviousNickSuggestions(payload, query);
        } catch(Exception e) { return new ArrayList<>(); }
    }

    private ArrayList<JSONObject> fetchLiveNickSuggestions(String query) {
        ArrayList<JSONObject> out = new ArrayList<>();
        String q = query == null ? "" : query.trim();
        if (q.length() < 2) return out;
        JSONObject official = validProfileObject(
                tryJson(habboApiUrl("/api/public/users?name=" + enc(q)))
        );
        if (official != null) out.add(official);
        return out;
    }

    private ArrayList<JSONObject> filterExactPreviousNickSuggestions(JSONObject suggest, String query) {
        ArrayList<JSONObject> out = new ArrayList<>();
        String q = normalizeNickKey(query);
        if (q.length() < 2 || suggest == null) return out;
        ArrayList<JSONObject> users = extractSuggestionUsers(suggest);
        for (JSONObject user : users) {
            String current = firstText(user, "name", "username", "habboName");
            String currentKey = normalizeNickKey(current);
            if (currentKey.isEmpty() || currentKey.equals(q)) continue;
            if (hasExactPreviousNick(user, q)) out.add(user);
            if (out.size() >= 6) break;
        }
        return out;
    }

    private boolean hasExactPreviousNick(JSONObject user, String normalizedQuery) {
        String q = normalizedQuery == null ? "" : normalizedQuery.trim().toLowerCase(Locale.ROOT);
        if (q.length() < 2) return false;
        return getExactPreviousNameMatch(user, q) != null;
    }

    private String stableSuggestionKey(JSONObject user) {
        String id = firstText(user, "uniqueId", "id", "habboId");
        if (!id.isEmpty()) return id;
        return normalizeNickKey(firstText(user, "name", "username", "habboName"));
    }

    private JSONObject getExactPreviousNameMatch(JSONObject user, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (JSONObject prev : extractPreviousNamesFromUser(user)) {
            String old = firstText(prev, "name", "oldName", "username");
            if (!old.isEmpty() && old.trim().toLowerCase(Locale.ROOT).equals(q)) return prev;
        }
        return null;
    }

    private LinearLayout suggestionRow(String query, JSONObject user, boolean compact) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(10), dp(8), dp(10), dp(8));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(24,255,255,255), dp(14), lightTheme ? Color.rgb(220,220,220) : Color.argb(30,255,255,255), 1));
        row.setLayoutParams(lp(-1, compact ? dp(68) : dp(82), 0, 0, 0, 8));
        FrameLayout headWrap = new FrameLayout(this);
        row.addView(headWrap, new LinearLayout.LayoutParams(dp(compact?50:58), dp(compact?54:62)));
        ImageView head = new ImageView(this);
        head.setScaleType(ImageView.ScaleType.FIT_CENTER);
        headWrap.addView(head, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));

        String name = firstText(user, "name", "username", "habboName");
        String fig = firstText(user, "figureString", "figure", "look");
        String uniqueId = firstText(user, "uniqueId", "id", "habboId");
        loadHeadImageForKnownProfile(head, fig, uniqueId, name, currentHotelKey);

        if (optBoolAny(user, false, "online", "isOnline")) {
            IconView dot = new IconView(this, "dot");
            FrameLayout.LayoutParams dpv = new FrameLayout.LayoutParams(dp(16), dp(16), Gravity.RIGHT | Gravity.TOP);
            dpv.topMargin = dp(2);
            dpv.rightMargin = dp(2);
            headWrap.addView(dot, dpv);
        }
        JSONObject previous = getExactPreviousNameMatch(user, query);
        String oldName = previous == null ? "" : firstText(previous, "name", "oldName", "username");
        String changed = previous == null ? "" : niceDate(firstText(previous, "changedAt", "date", "timestamp", "createdAt"));
        LinearLayout texts = new LinearLayout(this); texts.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1); tp.leftMargin = dp(10); row.addView(texts, tp);
        TextView nm = habboText(name, compact ? 15 : 17, true); nm.setMaxLines(1); nm.setEllipsize(TextUtils.TruncateAt.END); texts.addView(nm);
        if (previous != null && !oldName.isEmpty()) {
            TextView old = text(t(R.string.old_nick) + ": " + oldName, compact ? 12 : 13, Color.argb(210,255,255,255), false); old.setMaxLines(1); old.setEllipsize(TextUtils.TruncateAt.END); texts.addView(old);
            if (!changed.isEmpty() && !"—".equals(changed)) texts.addView(text(t(R.string.changed_at) + ": " + changed, compact ? 11 : 12, muted, false));
        }
        TextView arrow = text("›", compact ? 24 : 28, Color.WHITE, true); row.addView(arrow, new LinearLayout.LayoutParams(dp(26), -1));
        row.setOnClickListener(v -> openProfileReference(name, uniqueId, fig, currentHotelKey));
        return row;
    }

    private void showNotFoundState(String nick, ArrayList<JSONObject> suggestions) {
        startScreenVisible = false;
        updateStartNativeAdVisibility();
        resultWrap.removeAllViews();
        LinearLayout c = sectionCard(null, 0, false);
        c.setPadding(dp(18), dp(18), dp(18), dp(18));
        TextView title = habboText(t(R.string.no_profile_found), 22, true); title.setGravity(Gravity.CENTER); c.addView(title, lp(-1,-2,0,0,0,8));
        TextView body = text(tr(R.string.not_found_body, nick), 14, muted, false); body.setGravity(Gravity.CENTER); body.setLineSpacing(dp(2),1f); c.addView(body, lp(-1,-2,0,0,0,14));
        if (suggestions != null && !suggestions.isEmpty()) {
            TextView st = habboText(t(R.string.old_nick_suggestions_title), 17, true); c.addView(st, lp(-1,-2,0,0,0,10));
            for (JSONObject user : suggestions) c.addView(suggestionRow(nick, user, false));
        } else {
            c.addView(centerNote(t(R.string.no_old_nick_suggestions)));
        }
    }

    private void tintScrollBar(View v) {
        if (Build.VERSION.SDK_INT >= 29) {
            v.setVerticalScrollbarThumbDrawable(round(purple, dp(999), purple, 0));
            v.setVerticalScrollbarTrackDrawable(round(Color.argb(20,255,255,255), dp(999), Color.argb(20,255,255,255), 0));
        }
    }

    private void applyRoundedClip(View v, int radius) {
        if (Build.VERSION.SDK_INT >= 21) {
            v.setClipToOutline(true);
            v.setOutlineProvider(new ViewOutlineProvider() {
                @Override public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
        }
    }

    private void showError(String msg) {
        startScreenVisible = false;
        updateStartNativeAdVisibility();
        resultWrap.removeAllViews();
        LinearLayout c = sectionCard(t(R.string.error_title), 0, false);
        TextView message = text(msg, 15, Color.WHITE, true);
        message.setGravity(Gravity.CENTER);
        c.addView(message);
    }
    private void setStatusMessage(String message) {
        if (statusText == null) return;
        String clean = message == null ? "" : message.trim();
        statusText.setText(clean);
        statusText.setVisibility(clean.isEmpty() ? View.GONE : View.VISIBLE);
    }
    private void setLoading(boolean loading, String message) {
        if (loading) { suppressSuggestions = true; suggestionRequestId++; setSuggestionsVisible(false); }
        searchBtn.setEnabled(!loading);
        searchBtn.setText(loading ? t(R.string.searching_profile) : t(R.string.search_button));
        progress.setVisibility(View.GONE);
        setStatusMessage(loading ? "" : message);
        if (loading) showLoadingSkeleton(message == null ? t(R.string.searching_profile) : message);
    }

    private void showInlineLoading(String message) {
        inlineProgressMessage = message == null ? "" : message;
        inlineProgressPct = loadingProgressFor(message);
        setStatusMessage("");
    }

    private View inlineProgressBar(int pct) {
        FrameLayout bar = new FrameLayout(this);
        bar.setBackground(round(lightTheme ? Color.rgb(232,232,232) : Color.argb(34,255,255,255), dp(999), lightTheme ? Color.rgb(216,216,216) : Color.argb(28,255,255,255), 1));

        View fill = new View(this);
        fill.setBackground(grad(dp(999), purple2, purple));
        bar.setTag(fill);
        int available = Math.max(dp(80), getResources().getDisplayMetrics().widthPixels - dp(72));
        int bounded = Math.max(0, Math.min(100, pct));
        int width = bounded == 0
                ? 0
                : Math.max(dp(22), (int)(available * (bounded / 100f)));
        bar.addView(fill, new FrameLayout.LayoutParams(width, dp(9), Gravity.LEFT | Gravity.CENTER_VERTICAL));
        return bar;
    }

    private void updateLoadingSkeletonProgress(int token, int pct) {
        uiHandler.post(() -> {
            if (!isActiveToken(token) || !searchInProgress) return;
            FrameLayout bar = loadingSkeletonProgressBar;
            if (bar == null) return;
            Object tagged = bar.getTag();
            if (!(tagged instanceof View)) return;
            View fill = (View) tagged;
            int available = bar.getWidth();
            if (available <= 0) {
                available = Math.max(
                        dp(80),
                        getResources().getDisplayMetrics().widthPixels - dp(72)
                );
            }
            int bounded = Math.max(0, Math.min(100, pct));
            int width = bounded == 0
                    ? 0
                    : Math.max(dp(22), (int)(available * (bounded / 100f)));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    width,
                    dp(9),
                    Gravity.LEFT | Gravity.CENTER_VERTICAL
            );
            fill.setLayoutParams(params);
        });
    }

    
    private LinearLayout loadingProgressCard(String message, int pct) {
        LinearLayout card = card(dp(18));
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(row, lp(-1, -2, 0, 0, 0, 10));

        ProgressBar spinner = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        if (Build.VERSION.SDK_INT >= 21) {
            spinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(purple));
        }
        row.addView(spinner, new LinearLayout.LayoutParams(dp(30), dp(30)));

        TextView tv = text(message == null ? t(R.string.generic_loading) : message, 13, Color.argb(230,255,255,255), true);
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.leftMargin = dp(10);
        row.addView(tv, tp);

        card.addView(inlineProgressBar(Math.max(8, pct)), lp(-1, dp(8), 0, 0, 0, 0));
        return card;
    }

private int loadingProgressFor(String message) {
        String m = message == null ? "" : message.toLowerCase(Locale.ROOT);
        if (m.contains("detalhes")) return 20;
        if (m.contains("histórico") || m.contains("historico")) return 42;
        if (m.contains("visuais") || m.contains("amigos")) return 66;
        if (m.contains("quartos") || m.contains("grupos")) return 86;
        return 10;
    }

    private String loadingProfileAvatarUrl(String figure) {
        String clean = figure == null || figure.trim().isEmpty() ? "hd-6295" : figure.trim();
        return habboImagingUrl("/habbo-imaging/avatarimage?figure=" + enc(clean) + "&size=l&direction=2&head_direction=3&gesture=std&action=wav&headonly=0&img_format=png");
    }

    private void updateLoadingProfileFigureHint(final String figure, final int token) {
        final String clean = figure == null ? "" : figure.trim();
        if (clean.isEmpty()) return;
        loadingProfileFigureHint = clean;
        runOnUiThread(() -> {
            if (!isActiveToken(token) || !searchInProgress) return;
            ImageView target = loadingProfileAvatarImage;
            if (target == null) return;
            String url = loadingProfileAvatarUrl(clean);
            try {
                Glide.with(MainActivity.this).load(url).into(target);
            } catch (Exception ex) {
                loadImage(target, url);
            }
        });
    }

    private void showLoadingSkeleton(String message) {
        resultWrap.removeAllViews();

        LinearLayout c = card(dp(22));
        c.setPadding(dp(18), dp(18), dp(18), dp(18));
        resultWrap.addView(c, lp(-1, -2, 0, 0, 0, 18));

        TextView title = habboText(message, 18, true);
        title.setGravity(Gravity.CENTER);
        c.addView(title, lp(-1,-2,0,0,0,8));
        ProgressBar skeletonSpinner = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        if (Build.VERSION.SDK_INT >= 21) skeletonSpinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(purple));
        LinearLayout spinnerLine = new LinearLayout(this);
        spinnerLine.setGravity(Gravity.CENTER);
        spinnerLine.addView(skeletonSpinner, new LinearLayout.LayoutParams(dp(30), dp(30)));
        c.addView(spinnerLine, lp(-1, dp(34), 0,0,0,12));

        loadingSkeletonProgressBar = (FrameLayout) inlineProgressBar(
                Math.max(0, Math.min(100, inlineProgressPct))
        );
        c.addView(loadingSkeletonProgressBar, lp(-1, dp(9), 0, 0, 0, 16));

        FrameLayout avatar = new FrameLayout(this);
        avatar.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.rgb(15, 8, 25), dp(20), lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255), 1));
        c.addView(avatar, lp(-1, dp(280), 0,0,0,16));

        ImageView walker = new ImageView(this);
        loadingProfileAvatarImage = walker;
        walker.setScaleType(ImageView.ScaleType.FIT_CENTER);
        walker.setPadding(dp(20), dp(10), dp(20), dp(84));
        avatar.addView(walker, new FrameLayout.LayoutParams(-1, -1));
        String cachedFigure = loadingProfileFigureHint == null ? "" : loadingProfileFigureHint.trim();
        if (cachedFigure == null || cachedFigure.trim().isEmpty()) {
            // Figure neutra usada somente durante o loader quando ainda não sabemos a figure real.
            cachedFigure = "hd-6295";
        }
        // Nunca usa ?user=nick no loader: nicks repetidos podem carregar o avatar de outra conta.
        // Durante o loader, usa a pose solicitada para deixar o avatar pesquisado mais vivo.
        String walkerUrl = loadingProfileAvatarUrl(cachedFigure);
        String fallbackUrl = walkerUrl;
        try {
            Glide.with(this).load(walkerUrl).error(Glide.with(this).load(fallbackUrl)).into(walker);
        } catch (Exception ex) {
            loadImage(walker, fallbackUrl);
        }

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        c.addView(grid, lp(-1, -2, 0, 0, 0, 0));
        grid.addView(skeletonLine(dp(180), dp(28), true));
        grid.addView(skeletonLine(-1, dp(16), false));
        grid.addView(skeletonLine(-1, dp(16), false));

        for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            grid.addView(row, lp(-1, dp(58), 0, 6, 0, 8));
            for (int col = 0; col < 2; col++) {
                LinearLayout mini = new LinearLayout(this);
                mini.setOrientation(LinearLayout.HORIZONTAL);
                mini.setGravity(Gravity.CENTER_VERTICAL);
                mini.setPadding(dp(10), dp(7), dp(10), dp(7));
                mini.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(22,255,255,255), dp(16), lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255), 1));
                LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, -1, 1);
                if (col == 1) mp.leftMargin = dp(8);
                row.addView(mini, mp);
                mini.addView(skeletonBlock(dp(24), dp(24), dp(999)));
                LinearLayout lines = new LinearLayout(this);
                lines.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lpLines = new LinearLayout.LayoutParams(0, -2, 1);
                lpLines.leftMargin = dp(9);
                mini.addView(lines, lpLines);
                lines.addView(skeletonLine(dp(70), dp(10), false));
                lines.addView(skeletonLine(dp(110), dp(14), false));
            }
        }
    }

    private View skeletonLine(int width, int height, boolean centered) {
        View v = skeletonBlock(width < 0 ? -1 : width, height, dp(999));
        LinearLayout.LayoutParams p = lp(width < 0 ? -1 : width, height, centered ? 40 : 0, 0, centered ? 40 : 0, 10);
        v.setLayoutParams(p);
        return v;
    }

    private View skeletonBlock(int width, int height, int radius) {
        View v = new View(this);
        v.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(28,255,255,255), radius, lightTheme ? Color.rgb(220,220,220) : Color.argb(18,255,255,255), 1));
        v.setAlpha(0.72f);
        v.animate().alpha(1f).setDuration(650).withEndAction(() -> v.animate().alpha(0.55f).setDuration(650).withEndAction(() -> pulseSkeleton(v)).start()).start();
        v.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        return v;
    }

    private void pulseSkeleton(View v) {
        if (v == null || v.getWindowToken() == null) return;
        v.animate().alpha(1f).setDuration(650).withEndAction(() -> {
            if (v.getWindowToken() != null) v.animate().alpha(0.55f).setDuration(650).withEndAction(() -> pulseSkeleton(v)).start();
        }).start();
    }

    private void startFloating(View v) {
        if (v == null) return;
        v.setTranslationY(dp(5));
        v.animate().translationY(-dp(7)).setDuration(900).withEndAction(() -> {
            if (v.getWindowToken() != null) v.animate().translationY(dp(5)).setDuration(900).withEndAction(() -> startFloating(v)).start();
        }).start();
    }


    private String habbodexEndpointUrl(String uniqueId, String endpoint, int page, int limit) {
        return habbodexListUrl(uniqueId, endpoint, page, limit);
    }

    private String habbodexFigureUrl(String figure) {
        return HABBODEX_FURNIDEX_API
                + "?figureString=" + enc(figure)
                + "&hotel=" + enc(habbodexHotelCode(currentHotelKey));
    }

    private String habbodexListUrl(String uniqueId, String endpoint, int page, int limit) {
        return HABBODEX_BASE
                + "/" + enc(uniqueId)
                + "/" + enc(endpoint)
                + "?page=" + Math.max(1, page)
                + "&limit=" + Math.max(1, Math.min(100, limit));
    }

    private String habbodexProfileUrl(String uniqueId) {
        return HABBODEX_BASE + "/" + enc(uniqueId);
    }

    private String habbodexSuggestUrl(String name) {
        return habbodexSuggestUrl(name, currentHotelKey);
    }

    private String habbodexSuggestUrl(String name, String hotelKey) {
        return HABBODEX_BASE
                + "/habbos?name=" + enc(name)
                + "&includePreviousNames=true"
                + "&hotel=" + enc(habbodexHotelCode(hotelKey));
    }

    private JSONObject fetchDirectHabbodexSuggestions(String name) {
        return fetchDirectHabbodexSuggestions(name, currentHotelKey);
    }

    private JSONObject fetchDirectHabbodexSuggestions(String name, String hotelKey) {
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty()) return null;
        JSONObject direct = unwrap(tryJson(habbodexSuggestUrl(clean, hotelKey)));
        if (direct != null && (
                !extractSuggestionUsers(direct).isEmpty()
                || validProfileObject(direct) != null
        )) return direct;
        return direct;
    }

    private JSONObject suggestionsFromProfile(JSONObject profile) {
        if (profile == null) return null;
        try {
            JSONObject wrapped = new JSONObject();
            JSONArray users = new JSONArray();
            users.put(profile);
            wrapped.put("habbos", users);
            return wrapped;
        } catch(Exception ignored) {
            return null;
        }
    }

    private JSONObject fetchHabbodexSuggestions(String name) {
        return fetchHabbodexSuggestions(name, currentHotelKey);
    }

    private JSONObject fetchHabbodexSuggestions(String name, String hotelKey) {
        JSONObject direct = fetchDirectHabbodexSuggestions(name, hotelKey);
        if (direct != null) {
            if (!extractSuggestionUsers(direct).isEmpty()) return direct;
            JSONObject single = validProfileObject(direct);
            if (single != null) return suggestionsFromProfile(single);
        }
        return null;
    }

    private JSONObject resolveHabbodexProfileFromSuggestions(
            JSONObject suggestions,
            String query
    ) {
        if (suggestions == null) return null;
        String wanted = normalizeNickKey(query);
        JSONObject previousNameMatch = null;
        JSONObject currentNameMatch = null;
        for (JSONObject candidate : extractSuggestionUsers(suggestions)) {
            String currentName = normalizeNickKey(firstText(
                    candidate,
                    "name", "username", "habboName"
            ));
            if (!wanted.isEmpty() && wanted.equals(currentName)) {
                currentNameMatch = candidate;
                break;
            }
            if (previousNameMatch == null && hasExactPreviousNick(candidate, wanted)) {
                previousNameMatch = candidate;
            }
        }
        JSONObject candidate = currentNameMatch != null
                ? currentNameMatch
                : previousNameMatch;
        if (candidate == null) return null;

        String uniqueId = firstText(
                candidate, "uniqueId", "habboUniqueId", "id", "habboId"
        );
        JSONObject fullProfile = fetchDirectHabbodexProfile(uniqueId);
        return fullProfile != null ? fullProfile : validProfileObject(candidate);
    }

    private ArrayList<JSONObject> fetchPreferredPreviousNames(
            String uniqueId,
            String currentName
    ) {
        String cleanId = uniqueId == null ? "" : uniqueId.trim();
        String cleanName = currentName == null ? "" : currentName.trim();
        if (!cleanId.isEmpty()) {
            JSONObject direct = unwrap(tryJson(
                    habbodexListUrl(cleanId, "previous-names", 1, 100)
            ));
            if (direct != null) {
                ArrayList<JSONObject> names = extractList(direct, "previousNames");
                if (names.isEmpty()) names = extractList(direct, "names");
                if (!names.isEmpty()) return names;
            }

            JSONObject directProfile = fetchDirectHabbodexProfile(cleanId);
            ArrayList<JSONObject> profileNames = extractList(
                    directProfile,
                    "previousNames"
            );
            if (!profileNames.isEmpty()) return profileNames;

        }

        if (!cleanName.isEmpty()) {
            JSONObject suggestions = fetchHabbodexSuggestions(cleanName);
            return extractPreviousNamesFromSuggest(suggestions, cleanName);
        }
        return new ArrayList<>();
    }

    private JSONObject fetchDirectHabbodexProfile(String uniqueId) {
        String cleanId = uniqueId == null ? "" : uniqueId.trim();
        if (cleanId.isEmpty()) return null;
        JSONObject profile = extractHabbodexProfilePayload(
                unwrap(tryJson(habbodexProfileUrl(cleanId)))
        );
        if (profile == null || !isSameProfileId(cleanId, profile)) return null;
        // A coleção completa de emblemas não entra pelo payload de perfil; ela
        // usa a rota paginada /badges. selectedBadges permanece por ser pequena.
        profile.remove("badges");
        // selectedBadges é pequeno e útil para trazer Obtido em dos emblemas
        // selecionados sem baixar a coleção completa.
        profile.remove("totalBadges");
        profile.remove("badgeCount");
        profile.remove("badgesCount");
        profile.remove("badgesTotal");
        return profile;
    }

    private JSONObject fetchDirectHabbodexHistoricalComplement(
            String uniqueId,
            boolean includePrivate
    ) {
        String cleanId = uniqueId == null ? "" : uniqueId.trim();
        if (cleanId.isEmpty()) return null;

        String historicalSections = includePrivate
                ? "profile,previous-names,friends,previous-friends,previous-mottos,previous-styles,rooms,groups,photos"
                : "profile,previous-names,friends,previous-friends,previous-mottos,previous-styles";
        JSONObject batch = fetchHabbodexBatchDirect(
                cleanId,
                includePrivate,
                historicalSections,
                25
        );
        if (batch == null) {
            return fetchDirectHabbodexComplementBySections(cleanId, includePrivate);
        }

        boolean partial = batch.optBoolean("partial", false);
        JSONObject errors = batch.optJSONObject("errors");
        if (errors != null && errors.length() > 0) partial = true;

        JSONObject directProfile = batch.optJSONObject("profile");
        if (directProfile != null) {
            String returnedId = firstText(
                    directProfile, "uniqueId", "habboUniqueId", "id", "habboId"
            );
            if (!returnedId.isEmpty() && !normalizeNickKey(cleanId).equals(normalizeNickKey(returnedId))) {
                return null;
            }
        }

        JSONObject out;
        try {
            out = directProfile == null
                    ? new JSONObject()
                    : new JSONObject(directProfile.toString());
            copyJsonArray(batch, "friends", out, "friends");
            copyJsonArray(batch, "previousFriends", out, "previousFriends");
            copyJsonArray(batch, "previousNames", out, "previousNames");
            copyJsonArray(batch, "previousMottos", out, "previousMottos");
            copyJsonArray(batch, "previousStyles", out, "previousStyles");
            copyJsonArray(batch, "rooms", out, "rooms");
            copyJsonArray(batch, "groups", out, "groups");
            copyJsonArray(batch, "photos", out, "photos");

            // Compatibilidade com uma instalação antiga do proxy: somente se o
            // lote ainda não trouxer a seção, consulta a rota dedicada.
            if (!batch.has("previousFriends")) {
                JSONObject previousFriends = unwrap(tryJson(
                        habbodexListUrl(cleanId, "previous-friends", 1, 100)
                ));
                if (previousFriends == null) {
                    partial = true;
                } else {
                    ArrayList<JSONObject> removedFriends = extractList(previousFriends, "previousFriends");
                    if (removedFriends.isEmpty()) removedFriends = extractList(previousFriends, "friends");
                    out.put("previousFriends", jsonArrayFromObjects(removedFriends));
                }
            }

            if (includePrivate
                    && extractList(out, "friends").isEmpty()
                    && extractList(out, "rooms").isEmpty()
                    && extractList(out, "groups").isEmpty()
                    && extractList(out, "photos").isEmpty()) {
                // Alguns perfis privados retornam HTTP 200, porém todas as listas
                // vêm vazias. Mantém a marca de resposta parcial para a interface.
                partial = true;
            }

            out.put("_toxicHabbodexDirect", true);
            out.put("_toxicHabbodexPartial", partial);
            return out;
        } catch(Exception ignored) {
            return null;
        }
    }

    private JSONObject fetchDirectHabbodexComplementBySections(
            String uniqueId,
            boolean includePrivate
    ) {
        HashMap<String, Future<JSONObject>> requests = new HashMap<>();
        requests.put("profile", executor.submit(
                () -> unwrap(getJson(habbodexProfileUrl(uniqueId)))
        ));
        requests.put("previousNames", executor.submit(
                () -> unwrap(getJson(habbodexListUrl(uniqueId, "previous-names", 1, 100)))
        ));
        requests.put("friends", executor.submit(
                () -> unwrap(getJson(habbodexListUrl(uniqueId, "friends", 1, 100)))
        ));
        requests.put("previousFriends", executor.submit(
                () -> unwrap(getJson(habbodexListUrl(uniqueId, "previous-friends", 1, 100)))
        ));
        requests.put("previousMottos", executor.submit(
                () -> unwrap(getJson(habbodexListUrl(uniqueId, "previous-mottos", 1, 100)))
        ));
        requests.put("previousStyles", executor.submit(
                () -> unwrap(getJson(habbodexListUrl(uniqueId, "previous-styles", 1, 100)))
        ));
        if (includePrivate) {
            requests.put("rooms", executor.submit(
                    () -> unwrap(getJson(habbodexListUrl(uniqueId, "rooms", 1, 100)))
            ));
            requests.put("groups", executor.submit(
                    () -> unwrap(getJson(habbodexListUrl(uniqueId, "groups", 1, 100)))
            ));
            requests.put("photos", executor.submit(
                    () -> unwrap(getJson(habbodexListUrl(uniqueId, "photos", 1, 100)))
            ));
        }

        JSONObject out = new JSONObject();
        int succeeded = 0;
        int failed = 0;
        for (Map.Entry<String, Future<JSONObject>> entry : requests.entrySet()) {
            JSONObject response = null;
            try {
                response = entry.getValue().get();
            } catch(Exception error) {
                entry.getValue().cancel(true);
            }
            if (response == null) {
                failed++;
                continue;
            }
            succeeded++;
            try {
                String key = entry.getKey();
                if ("profile".equals(key)) {
                    JSONObject profile = validProfileObject(response);
                    if (profile != null) fillMissingJsonFields(out, profile);
                    continue;
                }
                String primaryKey = key;
                ArrayList<JSONObject> items = extractList(response, primaryKey);
                if (items.isEmpty() && "previousFriends".equals(key)) {
                    items = extractList(response, "friends");
                } else if (items.isEmpty() && "previousMottos".equals(key)) {
                    items = extractList(response, "mottos");
                } else if (items.isEmpty() && "previousStyles".equals(key)) {
                    items = extractList(response, "styles");
                } else if (items.isEmpty() && "previousNames".equals(key)) {
                    items = extractList(response, "names");
                }
                out.put(key, jsonArrayFromObjects(items));
            } catch(Exception ignored) {}
        }

        if (succeeded == 0 || out.length() == 0) return null;
        try {
            out.put("_toxicHabbodexDirect", true);
            out.put("_toxicHabbodexPartial", failed > 0);
        } catch(Exception ignored) {}
        return out;
    }

    private void copyJsonArray(JSONObject source, String sourceKey, JSONObject target, String targetKey) {
        if (source == null || target == null) return;
        JSONArray array = source.optJSONArray(sourceKey);
        if (array == null) return;
        try { target.put(targetKey, array); } catch(Exception ignored) {}
    }

    private JSONArray jsonArrayFromObjects(ArrayList<JSONObject> items) {
        JSONArray out = new JSONArray();
        if (items != null) for (JSONObject item : items) if (item != null) out.put(item);
        return out;
    }

    private JSONObject mergeComplementPayloads(JSONObject primary, JSONObject fallback) {
        if (primary == null) return fallback;
        if (fallback == null) return primary;
        try {
            JSONObject merged = new JSONObject(primary.toString());
            fillMissingJsonFields(merged, fallback);
            String[] listKeys = new String[]{
                    "previousNames", "previousMottos", "previousStyles", "previousFriends",
                    "friends", "rooms", "groups", "photos"
            };
            for (String key : listKeys) {
                ArrayList<JSONObject> combined = mergeListsEnrichingPrimary(
                        extractList(primary, key),
                        extractList(fallback, key),
                        true
                );
                if (!combined.isEmpty() || primary.has(key) || fallback.has(key)) {
                    merged.put(key, jsonArrayFromObjects(combined));
                }
            }
            return merged;
        } catch(Exception ignored) {
            return primary;
        }
    }

    private JSONObject fetchPreferredHistoricalComplement(
            String uniqueId,
            boolean includePrivate,
            JSONObject existingFallback
    ) {
        return fetchDirectHabbodexHistoricalComplement(
                uniqueId,
                includePrivate
        );
    }

    private boolean isDirectHabbodexUrl(String u) {
        if (u == null) return false;
        return u.startsWith(HABBODEX_BASE) || u.startsWith(HABBODEX_FURNIDEX_API);
    }

    private boolean isFiveMinuteJsonCacheUrl(String u) {
        if (u == null || u.trim().isEmpty()) return false;
        if (isDirectHabbodexUrl(u)) return true;
        // PROFILE_API continua somente para recursos próprios do app
        // (assinatura/patrocinadores), portanto não entra no cache de perfil.
        return u.contains("habbo.com.br/api/public/")
                || u.contains("habbo.com/api/public/")
                || u.contains("habbo.es/api/public/")
                || u.contains("habbo.de/api/public/")
                || u.contains("habbo.fr/api/public/")
                || u.contains("habbo.fi/api/public/")
                || u.contains("habbo.it/api/public/")
                || u.contains("habbo.nl/api/public/")
                || u.contains("habbo.com.tr/api/public/")
                || u.contains("/extradata/public/users/");
    }

    private Object parseCachedJsonBody(String body) throws Exception {
        String clean = body == null ? "" : body.trim();
        if (clean.isEmpty()) throw new IOException("Empty cached JSON");
        if (clean.startsWith("<")) throw new IOException("HTML received instead of JSON");
        return clean.startsWith("[") ? new JSONArray(clean) : new JSONObject(clean);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(
                    (value == null ? "" : value).getBytes("UTF-8")
            );
            StringBuilder out = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) out.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            return out.toString();
        } catch(Exception ignored) {
            return Integer.toHexString((value == null ? "" : value).hashCode());
        }
    }

    private File fiveMinuteJsonCacheFile(String url) {
        File dir = new File(getCacheDir(), "profile_json_5m");
        if (!dir.exists()) {
            try { dir.mkdirs(); } catch(Exception ignored) {}
        }
        return new File(dir, sha256Hex(url) + ".json");
    }

    private String readFiveMinuteJsonDiskCache(String url) {
        try {
            File file = fiveMinuteJsonCacheFile(url);
            if (!file.isFile()) return null;
            long age = System.currentTimeMillis() - file.lastModified();
            if (age < 0L || age > JSON_RESPONSE_CACHE_TTL_MS) {
                file.delete();
                return null;
            }
            try (FileInputStream input = new FileInputStream(file)) {
                return readAll(input);
            }
        } catch(Exception ignored) {
            return null;
        }
    }

    private void writeFiveMinuteJsonDiskCache(String url, String body) {
        if (body == null || body.trim().isEmpty()) return;
        try {
            File file = fiveMinuteJsonCacheFile(url);
            File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
            try (FileOutputStream output = new FileOutputStream(tmp, false)) {
                output.write(body.getBytes("UTF-8"));
                output.flush();
            }
            if (!tmp.renameTo(file)) {
                try (FileOutputStream output = new FileOutputStream(file, false)) {
                    output.write(body.getBytes("UTF-8"));
                }
                tmp.delete();
            }
        } catch(Exception ignored) {}
    }

    private boolean isAllowedHabbodexWebHost(String host) {
        if (host == null) return false;
        String clean = host.toLowerCase(Locale.ROOT);
        return "habbodex.com".equals(clean)
                || "www.habbodex.com".equals(clean)
                || "challenges.cloudflare.com".equals(clean);
    }

    private String decodeJavascriptResult(String value) {
        if (value == null || "null".equals(value) || "undefined".equals(value)) return "";
        try {
            Object parsed = new JSONTokener(value).nextValue();
            return parsed == null || parsed == JSONObject.NULL ? "" : String.valueOf(parsed);
        } catch(Exception ignored) {
            return value;
        }
    }

    private boolean looksLikeHabbodexChallenge(String title, String text, String url) {
        String combined = ((title == null ? "" : title) + "\n"
                + (text == null ? "" : text) + "\n"
                + (url == null ? "" : url)).toLowerCase(Locale.ROOT);
        return combined.contains("just a moment")
                || combined.contains("checking your browser")
                || combined.contains("verify you are human")
                || combined.contains("verifying you are human")
                || combined.contains("performing security verification")
                || combined.contains("enable javascript and cookies to continue")
                || combined.contains("cf-chl-");
    }

    private void ensureHabbodexWebViewCreatedOnUiThread() {
        if (habbodexWebView != null || isFinishing()) return;

        WebView web = new WebView(this);
        habbodexWebView = web;
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        if (Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }

        android.webkit.CookieManager cookies = android.webkit.CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 21) cookies.setAcceptThirdPartyCookies(web, true);

        web.setFocusable(false);
        web.setFocusableInTouchMode(false);
        web.setBackgroundColor(Color.TRANSPARENT);
        web.addJavascriptInterface(new HabbodexJavascriptBridge(), "ToxicNative");
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                try {
                    Uri uri = request == null ? null : request.getUrl();
                    return uri != null && !isAllowedHabbodexWebHost(uri.getHost());
                } catch(Exception ignored) {
                    return true;
                }
            }

            @SuppressWarnings("deprecation")
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    Uri uri = Uri.parse(url == null ? "" : url);
                    return !isAllowedHabbodexWebHost(uri.getHost());
                } catch(Exception ignored) {
                    return true;
                }
            }

            @Override public void onPageFinished(WebView view, String url) {
                inspectHabbodexWebPage(view, url);
            }

            @Override public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error
            ) {
                if (request != null && request.isForMainFrame()) {
                    synchronized (habbodexWebSessionLock) {
                        CompletableFuture<Boolean> future = habbodexWebSessionFuture;
                        if (future != null && !future.isDone()) future.complete(false);
                    }
                }
            }
        });

        attachHabbodexWebViewHidden();
        resetHabbodexWebSessionFuture();
        web.loadUrl("https://habbodex.com/");
    }

    private void attachHabbodexWebViewHidden() {
        if (habbodexWebView == null || screen == null) return;
        if (habbodexVerificationDialog != null && habbodexVerificationDialog.isShowing()) return;
        try {
            detachViewFromParent(habbodexWebView);
            if (habbodexWebHiddenHost == null) {
                habbodexWebHiddenHost = new FrameLayout(this);
                habbodexWebHiddenHost.setClickable(false);
                habbodexWebHiddenHost.setFocusable(false);
                habbodexWebHiddenHost.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
                habbodexWebHiddenHost.setAlpha(0.01f);
            }
            detachViewFromParent(habbodexWebHiddenHost);
            habbodexWebHiddenHost.removeAllViews();
            habbodexWebHiddenHost.addView(
                    habbodexWebView,
                    new FrameLayout.LayoutParams(dp(1), dp(1))
            );
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    dp(1), dp(1), Gravity.BOTTOM | Gravity.RIGHT
            );
            screen.addView(habbodexWebHiddenHost, lp);
        } catch(Exception ignored) {}
    }

    private CompletableFuture<Boolean> resetHabbodexWebSessionFuture() {
        synchronized (habbodexWebSessionLock) {
            habbodexWebSessionFuture = new CompletableFuture<>();
            habbodexWebChallengeDetected = false;
            return habbodexWebSessionFuture;
        }
    }

    private void inspectHabbodexWebPage(WebView view, String url) {
        if (view == null) return;
        String script = "(function(){try{return JSON.stringify({href:String(location.href||''),"
                + "title:String(document.title||''),text:String(document.body?document.body.innerText:'').slice(0,1600)});"
                + "}catch(e){return JSON.stringify({href:'',title:'',text:''});}})();";
        try {
            view.evaluateJavascript(script, raw -> {
                String decoded = decodeJavascriptResult(raw);
                String href = url == null ? "" : url;
                String title = "";
                String text = "";
                try {
                    JSONObject state = new JSONObject(decoded);
                    href = state.optString("href", href);
                    title = state.optString("title", "");
                    text = state.optString("text", "");
                } catch(Exception ignored) {}

                boolean challenge = looksLikeHabbodexChallenge(title, text, href);
                if (challenge) {
                    habbodexWebChallengeDetected = true;
                    habbodexWebLastChallengeUrl = href;
                    showHabbodexVerificationDialog(href);
                    return;
                }

                Uri uri = null;
                try { uri = Uri.parse(href); } catch(Exception ignored) {}
                if (uri != null && isAllowedHabbodexWebHost(uri.getHost())
                        && !"challenges.cloudflare.com".equalsIgnoreCase(uri.getHost())) {
                    // Depois de uma verificação feita diretamente em /api/..., volta
                    // para uma página HTML do mesmo domínio antes de executar fetch().
                    String trimmedText = text == null ? "" : text.trim();
                    if (href.contains("/api/")
                            && (trimmedText.startsWith("{") || trimmedText.startsWith("["))
                            && habbodexVerificationDialog != null
                            && habbodexVerificationDialog.isShowing()) {
                        try { view.loadUrl("https://habbodex.com/"); } catch(Exception ignored) {}
                        return;
                    }

                    habbodexWebChallengeDetected = false;
                    try { android.webkit.CookieManager.getInstance().flush(); } catch(Exception ignored) {}
                    synchronized (habbodexWebSessionLock) {
                        CompletableFuture<Boolean> future = habbodexWebSessionFuture;
                        boolean alreadyReady = false;
                        try {
                            alreadyReady = future != null && future.isDone()
                                    && Boolean.TRUE.equals(future.getNow(false));
                        } catch(Exception ignored) {}
                        if (!alreadyReady) {
                            if (future == null || future.isDone()) {
                                future = new CompletableFuture<>();
                                habbodexWebSessionFuture = future;
                            }
                            future.complete(true);
                        }
                    }
                    if (habbodexVerificationDialog != null && habbodexVerificationDialog.isShowing()) {
                        try { habbodexVerificationDialog.dismiss(); } catch(Exception ignored) {}
                    }
                }
            });
        } catch(Exception ignored) {}
    }

    private boolean ensureHabbodexWebSession() throws Exception {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("HabboDex WebView transport cannot block the UI thread");
        }

        CompletableFuture<Boolean> created = new CompletableFuture<>();
        uiHandler.post(() -> {
            try {
                ensureHabbodexWebViewCreatedOnUiThread();
                created.complete(habbodexWebView != null);
            } catch(Exception error) {
                created.completeExceptionally(error);
            }
        });
        if (!Boolean.TRUE.equals(created.get(5, TimeUnit.SECONDS))) return false;

        CompletableFuture<Boolean> session;
        boolean reload = false;
        synchronized (habbodexWebSessionLock) {
            session = habbodexWebSessionFuture;
            if (session == null) {
                session = new CompletableFuture<>();
                habbodexWebSessionFuture = session;
                reload = true;
            } else if (session.isDone()) {
                boolean ready = false;
                try { ready = Boolean.TRUE.equals(session.getNow(false)); } catch(Exception ignored) {}
                if (!ready) {
                    session = new CompletableFuture<>();
                    habbodexWebSessionFuture = session;
                    reload = true;
                }
            }
        }
        if (reload) {
            uiHandler.post(() -> {
                try { if (habbodexWebView != null) habbodexWebView.loadUrl("https://habbodex.com/"); }
                catch(Exception ignored) {}
            });
        }
        try {
            return Boolean.TRUE.equals(session.get(HABBODEX_WEB_BOOT_TIMEOUT_MS, TimeUnit.MILLISECONDS));
        } catch(TimeoutException timeout) {
            uiHandler.post(() -> showHabbodexVerificationDialog(
                    habbodexWebLastChallengeUrl == null || habbodexWebLastChallengeUrl.trim().isEmpty()
                            ? "https://habbodex.com/"
                            : habbodexWebLastChallengeUrl
            ));
            try {
                return Boolean.TRUE.equals(session.get(
                        HABBODEX_WEB_INTERACTIVE_TIMEOUT_MS,
                        TimeUnit.MILLISECONDS
                ));
            } catch(TimeoutException ignored) {
                return false;
            }
        }
    }

    private CompletableFuture<Boolean> beginHabbodexInteractiveVerification(String url) {
        final CompletableFuture<Boolean> session = resetHabbodexWebSessionFuture();
        final String target = url == null || url.trim().isEmpty()
                ? "https://habbodex.com/"
                : url;
        habbodexWebLastChallengeUrl = target;
        uiHandler.post(() -> {
            ensureHabbodexWebViewCreatedOnUiThread();
            showHabbodexVerificationDialog(target);
            try { habbodexWebView.loadUrl(target); } catch(Exception ignored) {}
        });
        return session;
    }

    private void showHabbodexVerificationDialog(String targetUrl) {
        if (isFinishing() || habbodexWebView == null) return;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            uiHandler.post(() -> showHabbodexVerificationDialog(targetUrl));
            return;
        }
        if (habbodexVerificationDialog != null && habbodexVerificationDialog.isShowing()) return;

        try {
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            habbodexVerificationDialog = dialog;
            LinearLayout shell = new LinearLayout(this);
            shell.setOrientation(LinearLayout.VERTICAL);
            shell.setPadding(dp(12), dp(10), dp(12), dp(12));
            shell.setBackground(round(
                    lightTheme ? Color.WHITE : Color.rgb(18, 15, 25),
                    dp(20),
                    lightTheme ? Color.rgb(220, 215, 225) : Color.rgb(60, 52, 72),
                    1
            ));

            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);
            TextView title = text("Verificação HabboDex", 16,
                    lightTheme ? Color.rgb(35, 30, 40) : Color.WHITE, true);
            top.addView(title, new LinearLayout.LayoutParams(0, dp(44), 1f));
            TextView close = text("×", 28,
                    lightTheme ? Color.rgb(55, 50, 60) : Color.WHITE, false);
            close.setGravity(Gravity.CENTER);
            top.addView(close, new LinearLayout.LayoutParams(dp(44), dp(44)));
            shell.addView(top, new LinearLayout.LayoutParams(-1, dp(44)));

            TextView hint = text(
                    "Conclua a verificação exibida pelo HabboDex. Depois o aplicativo continua automaticamente.",
                    12,
                    lightTheme ? Color.rgb(90, 82, 98) : Color.argb(210,255,255,255),
                    false
            );
            hint.setPadding(dp(2), 0, dp(2), dp(8));
            shell.addView(hint, new LinearLayout.LayoutParams(-1, -2));

            FrameLayout browserHost = new FrameLayout(this);
            browserHost.setBackgroundColor(lightTheme ? Color.WHITE : Color.rgb(10,10,12));
            shell.addView(browserHost, new LinearLayout.LayoutParams(-1, 0, 1f));

            detachViewFromParent(habbodexWebView);
            browserHost.addView(habbodexWebView, new FrameLayout.LayoutParams(-1, -1));
            habbodexWebView.setFocusable(true);
            habbodexWebView.setFocusableInTouchMode(true);

            close.setOnClickListener(v -> dialog.dismiss());
            dialog.setContentView(shell);
            dialog.setOnDismissListener(d -> {
                if (habbodexVerificationDialog == dialog) habbodexVerificationDialog = null;
                try {
                    if (habbodexWebView != null) {
                        habbodexWebView.setFocusable(false);
                        habbodexWebView.setFocusableInTouchMode(false);
                    }
                } catch(Exception ignored) {}
                attachHabbodexWebViewHidden();
            });
            dialog.setOnShowListener(d -> {
                Window w = dialog.getWindow();
                if (w != null) {
                    w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    w.setLayout(-1, -1);
                }
            });
            dialog.show();
            Window w = dialog.getWindow();
            if (w != null) {
                w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                w.setLayout(-1, -1);
            }
            String target = targetUrl == null || targetUrl.trim().isEmpty()
                    ? "https://habbodex.com/"
                    : targetUrl;
            try {
                String current = habbodexWebView.getUrl();
                if (current == null || current.trim().isEmpty()
                        || looksLikeHabbodexChallenge("", "", current)) {
                    habbodexWebView.loadUrl(target);
                }
            } catch(Exception ignored) {}
        } catch(Exception ignored) {
            attachHabbodexWebViewHidden();
        }
    }

    private class HabbodexJavascriptBridge {
        @JavascriptInterface
        public void deliver(String token, String requestId, String payload) {
            if (!habbodexWebBridgeToken.equals(token) || requestId == null) return;
            CompletableFuture<String> future = habbodexWebRequests.remove(requestId);
            if (future != null && !future.isDone()) {
                future.complete(payload == null ? "" : payload);
            }
        }
    }

    private Object fetchHabbodexViaWebViewOnce(String url) throws Exception {
        if (!ensureHabbodexWebSession()) {
            throw new IOException("HabboDex web session unavailable");
        }

        String requestId = "tx" + habbodexWebRequestSeq.incrementAndGet()
                + "_" + UUID.randomUUID().toString();
        CompletableFuture<String> future = new CompletableFuture<>();
        habbodexWebRequests.put(requestId, future);

        String quotedToken = JSONObject.quote(habbodexWebBridgeToken);
        String quotedId = JSONObject.quote(requestId);
        String quotedUrl = JSONObject.quote(url);
        String script = "(function(){var token=" + quotedToken + ";var id=" + quotedId + ";var u=" + quotedUrl + ";"
                + "var send=function(o){try{ToxicNative.deliver(token,id,JSON.stringify(o));}catch(e){}};"
                + "var isBadgeList=/\\/badges(?:\\?|$)/i.test(u);"
                + "var scrub=function(o,d){if(!o||typeof o!=='object'||d>3)return;"
                + "['badges','badgeList','achievementBadges','achievements'].forEach(function(k){try{if(Object.prototype.hasOwnProperty.call(o,k))delete o[k];}catch(e){}});"
                + "['data','profile','user','habbo','result'].forEach(function(k){try{scrub(o[k],d+1);}catch(e){}});};"
                + "fetch(u,{method:'GET',credentials:'include',cache:'no-store',headers:{'Accept':'application/json,text/plain,*/*'}})"
                + ".then(function(r){return r.text().then(function(t){var body=t;try{var j=JSON.parse(t);if(!isBadgeList)scrub(j,0);body=JSON.stringify(j);}catch(e){}"
                + "send({ok:r.ok,status:r.status,url:r.url||u,contentType:r.headers.get('content-type')||'',body:body});});})"
                + ".catch(function(e){send({ok:false,status:0,url:u,error:String(e),body:''});});})();";

        uiHandler.post(() -> {
            try {
                if (habbodexWebView == null) {
                    CompletableFuture<String> pending = habbodexWebRequests.remove(requestId);
                    if (pending != null) pending.completeExceptionally(new IOException("WebView unavailable"));
                    return;
                }
                habbodexWebView.evaluateJavascript(script, null);
            } catch(Exception error) {
                CompletableFuture<String> pending = habbodexWebRequests.remove(requestId);
                if (pending != null) pending.completeExceptionally(error);
            }
        });

        String envelopeText;
        try {
            envelopeText = future.get(HABBODEX_WEB_REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } finally {
            habbodexWebRequests.remove(requestId);
        }
        JSONObject envelope = new JSONObject(envelopeText == null || envelopeText.trim().isEmpty()
                ? "{}" : envelopeText);
        int status = envelope.optInt("status", 0);
        boolean ok = envelope.optBoolean("ok", false);
        String body = envelope.optString("body", "");
        String error = envelope.optString("error", "");

        if (ok && status >= 200 && status < 300 && !body.trim().isEmpty()) {
            if (body.trim().startsWith("<") || looksLikeHabbodexChallenge("", body, url)) {
                throw new HabbodexWebChallengeException(url, status);
            }
            return parseCachedJsonBody(body);
        }

        if (status == 403 || status == 429 || status == 503
                || body.trim().startsWith("<")
                || looksLikeHabbodexChallenge("", body, url)) {
            throw new HabbodexWebChallengeException(url, status);
        }
        throw new IOException(error.isEmpty() ? "HabboDex HTTP " + status : error);
    }

    private Object getJsonViaHabbodexWebView(String url) throws Exception {
        try {
            return fetchHabbodexViaWebViewOnce(url);
        } catch(HabbodexWebChallengeException challenge) {
            CompletableFuture<Boolean> session = beginHabbodexInteractiveVerification(challenge.url);
            boolean verified;
            try {
                verified = Boolean.TRUE.equals(session.get(
                        HABBODEX_WEB_INTERACTIVE_TIMEOUT_MS,
                        TimeUnit.MILLISECONDS
                ));
            } catch(TimeoutException timeout) {
                verified = false;
            }
            if (!verified) throw challenge;
            return fetchHabbodexViaWebViewOnce(url);
        }
    }

    private static class HabbodexWebChallengeException extends IOException {
        final String url;
        final int status;

        HabbodexWebChallengeException(String url, int status) {
            super("HabboDex verification required (HTTP " + status + ")");
            this.url = url == null ? "https://habbodex.com/" : url;
            this.status = status;
        }
    }

    private void destroyHabbodexWebTransport() {
        try {
            if (habbodexVerificationDialog != null) habbodexVerificationDialog.dismiss();
        } catch(Exception ignored) {}
        habbodexVerificationDialog = null;

        IOException destroyed = new IOException("Activity destroyed");
        for (CompletableFuture<String> future : habbodexWebRequests.values()) {
            if (future != null && !future.isDone()) future.completeExceptionally(destroyed);
        }
        habbodexWebRequests.clear();

        try {
            if (habbodexWebView != null) {
                detachViewFromParent(habbodexWebView);
                habbodexWebView.removeJavascriptInterface("ToxicNative");
                habbodexWebView.stopLoading();
                habbodexWebView.loadUrl("about:blank");
                habbodexWebView.clearHistory();
                habbodexWebView.destroy();
            }
        } catch(Exception ignored) {}
        habbodexWebView = null;
        try { detachViewFromParent(habbodexWebHiddenHost); } catch(Exception ignored) {}
        habbodexWebHiddenHost = null;
    }

    private Object getJsonAny(String u) throws Exception {
        final boolean cacheable = isFiveMinuteJsonCacheUrl(u);
        final long now = SystemClock.elapsedRealtime();
        if (cacheable) {
            CachedJsonResponse cached = jsonResponseCache.get(u);
            if (cached != null) {
                if (now - cached.storedAtMs <= JSON_RESPONSE_CACHE_TTL_MS) {
                    try { return parseCachedJsonBody(cached.body); }
                    catch(Exception ignored) { jsonResponseCache.remove(u, cached); }
                } else {
                    jsonResponseCache.remove(u, cached);
                }
            }

            String diskBody = readFiveMinuteJsonDiskCache(u);
            if (diskBody != null && !diskBody.trim().isEmpty()) {
                try {
                    Object parsed = parseCachedJsonBody(diskBody);
                    jsonResponseCache.put(
                            u,
                            new CachedJsonResponse(diskBody, SystemClock.elapsedRealtime())
                    );
                    return parsed;
                } catch(Exception ignored) {
                    try { fiveMinuteJsonCacheFile(u).delete(); } catch(Exception ignoredAgain) {}
                }
            }
        }

        // HabboDex não usa mais HttpURLConnection. A requisição acontece dentro de
        // uma sessão WebView real do próprio aparelho, preservando cookies/JS/origem.
        if (isDirectHabbodexUrl(u)) {
            Object parsed = getJsonViaHabbodexWebView(u);
            if (cacheable && parsed != null) {
                String body = parsed.toString();
                jsonResponseCache.put(
                        u,
                        new CachedJsonResponse(body, SystemClock.elapsedRealtime())
                );
                writeFiveMinuteJsonDiskCache(u, body);
            }
            return parsed;
        }

        final boolean ownServer = u != null && u.startsWith(PROFILE_API);
        final boolean largeOfficialProfile = u != null
                && u.contains("/api/public/users/")
                && u.endsWith("/profile");
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection)new URL(u).openConnection();
            c.setUseCaches(false);
            c.setDefaultUseCaches(false);
            c.setConnectTimeout(ownServer ? 6000 : 5000);
            c.setReadTimeout(ownServer ? 12000 : (largeOfficialProfile ? 15000 : 8000));
            c.setRequestProperty("Accept", "application/json, text/plain, */*");
            c.setRequestProperty(
                    "User-Agent",
                    "ToxicSearchTool/" + APP_VERSION + " Android (+https://atoxic.com.br)"
            );
            c.setRequestProperty("X-Toxic-App", APP_VERSION);

            int code = c.getResponseCode();
            InputStream is = code >= 200 && code < 300
                    ? c.getInputStream()
                    : c.getErrorStream();
            String body = readAll(is);

            if (code < 200 || code >= 300 || body == null || body.trim().isEmpty()) {
                throw new IOException("HTTP " + code);
            }

            Object parsed = parseCachedJsonBody(body);
            if (cacheable) {
                jsonResponseCache.put(
                        u,
                        new CachedJsonResponse(body, SystemClock.elapsedRealtime())
                );
                writeFiveMinuteJsonDiskCache(u, body);
            }
            return parsed;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private JSONObject getJson(String u) throws Exception { Object any = getJsonAny(u); if (any instanceof JSONObject) return (JSONObject)any; JSONObject wrap = new JSONObject(); wrap.put("data", any); return wrap; }
    private JSONObject tryJson(String u) { try { return getJson(u); } catch (Exception e) { return null; } }

    private static class ApiHttpException extends IOException {
        final int statusCode;
        final JSONObject payload;

        ApiHttpException(int statusCode, JSONObject payload) {
            super(payload == null ? "HTTP " + statusCode : payload.optString("error", "HTTP " + statusCode));
            this.statusCode = statusCode;
            this.payload = payload == null ? new JSONObject() : payload;
        }
    }

    private JSONObject postJsonObject(String url, JSONObject payload) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Cache-Control", "no-cache, no-store");
            connection.setRequestProperty("User-Agent", "ToxicSearchTool/" + APP_VERSION + " Android (+https://atoxic.com.br)");
            connection.setRequestProperty("X-Toxic-App", APP_VERSION);
            byte[] bytes = (payload == null ? "{}" : payload.toString()).getBytes("UTF-8");
            connection.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(bytes);
            }
            int code = connection.getResponseCode();
            InputStream input = code >= 200 && code < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readAll(input).trim();
            JSONObject response = body.isEmpty() ? new JSONObject() : new JSONObject(body);
            if (code < 200 || code >= 300) throw new ApiHttpException(code, response);
            return response;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
    private String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[32768];
        try {
            int n;
            while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } finally {
            try { is.close(); } catch(Exception ignored) {}
        }
    }
    private void loadImage(ImageView view, String url) { 
        if (view == null || url == null || url.trim().isEmpty()) return; 
        String clean = normalizeUrl(url); 
        runOnUiThread(() -> Glide.with(MainActivity.this).load(clean).into(view)); 
    }

    private void loadHeadImage(ImageView view, String url) {
        if (view == null) return;
        view.setImageResource(R.drawable.pre_load_head);
        if (url == null || url.trim().isEmpty()) return;
        String clean = normalizeUrl(url);
        runOnUiThread(() -> Glide.with(MainActivity.this)
            .load(clean)
            .placeholder(R.drawable.pre_load_head)
            .error(R.drawable.pre_load_head)
            .into(view));
    }

    private void loadHeadImageForKnownProfile(ImageView view, String figure, String uniqueId, String fallbackNick, String hotelKey) {
        if (view == null) return;
        view.setImageResource(R.drawable.pre_load_head);
        String fig = figure == null ? "" : figure.trim();
        if (!fig.isEmpty()) {
            loadHeadImage(view, avatarHead(fig));
            return;
        }
        String id = uniqueId == null ? "" : uniqueId.trim();
        String hotel = normalizeHotelKey(hotelKey);
        if (hotel.isEmpty()) hotel = currentHotelKey;
        if (!id.isEmpty()) {
            final String finalHotel = hotel;
            executor.execute(() -> {
                try {
                    JSONObject officialUser = validProfileObject(tryJson(
                            "https://" + hotelDomain(finalHotel) + "/api/public/users/" + enc(id)
                    ));
                    String fetchedFigure = firstText(officialUser, "figureString", "figure", "figure_string");
                    if (!fetchedFigure.isEmpty()) {
                        final String resolvedFigure = fetchedFigure;
                        runOnUiThread(() -> loadHeadImage(view, avatarHead(resolvedFigure)));
                    } else if (fallbackNick != null && !fallbackNick.trim().isEmpty()) {
                        runOnUiThread(() -> loadHeadImage(view, avatarHeadByNameForHotel(fallbackNick.trim(), finalHotel)));
                    }
                } catch(Exception ignored) {}
            });
            return;
        }
        // Só usa nick como último fallback, quando o dado não possui ID.
        if (fallbackNick != null && !fallbackNick.trim().isEmpty()) {
            loadHeadImage(view, avatarHeadByNameForHotel(fallbackNick.trim(), hotel));
        }
    }

    private void loadAvatarImage(ImageView view, String url) {
        if (view == null) return;
        view.setImageResource(R.drawable.pre_load);
        if (url == null || url.trim().isEmpty()) return;
        String clean = normalizeUrl(url);
        runOnUiThread(() -> Glide.with(MainActivity.this)
            .load(clean)
            .placeholder(R.drawable.pre_load)
            .error(R.drawable.pre_load)
            .into(view));
    }

    private void loadAvatarImageKeepingCurrent(ImageView view, String url) {
        if (view == null || url == null || url.trim().isEmpty()) return;
        String clean = normalizeUrl(url);
        runOnUiThread(() -> {
            Drawable current = view.getDrawable();
            if (current != null) {
                Glide.with(MainActivity.this)
                        .load(clean)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(current)
                        .error(current)
                        .dontAnimate()
                        .into(view);
            } else {
                Glide.with(MainActivity.this)
                        .load(clean)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .error(new ColorDrawable(Color.TRANSPARENT))
                        .dontAnimate()
                        .into(view);
            }
        });
    }

    private boolean resolveBannedFromHistoricalData(JSONObject complement) {
        if (complement == null) return false;

        Boolean explicit = optBoolNullableDeep(
                complement,
                "isBanned", "banned", "is_banned", "ban"
        );
        if (explicit != null) return explicit;

        String status = firstText(
                complement,
                "status", "accountStatus"
        ).toLowerCase(Locale.ROOT);
        return status.contains("banned")
                || status.contains("banido")
                || status.contains("banida");
    }
    private boolean hasProfileIdentity(JSONObject obj) {
        return obj != null && !firstText(
                obj,
                "uniqueId", "habboUniqueId", "id", "habboId",
                "name", "username", "habboName",
                "figureString", "figure"
        ).isEmpty();
    }

    private void addProfileCandidate(
            ArrayList<JSONObject> candidates,
            JSONObject candidate
    ) {
        if (candidate == null || candidates == null) return;
        for (JSONObject existing : candidates) if (existing == candidate) return;
        candidates.add(candidate);
    }

    private JSONObject extractHabbodexProfilePayload(JSONObject payload) {
        JSONObject root = unwrap(payload);
        if (root == null) return null;

        ArrayList<JSONObject> candidates = new ArrayList<>();
        JSONObject data = root.optJSONObject("data");
        JSONObject nestedPayload = root.optJSONObject("payload");
        addProfileCandidate(candidates, data == null ? null : data.optJSONObject("profile"));
        addProfileCandidate(candidates, root.optJSONObject("profile"));
        addProfileCandidate(candidates, data);
        addProfileCandidate(candidates, root.optJSONObject("user"));
        addProfileCandidate(candidates, root.optJSONObject("habbo"));
        addProfileCandidate(candidates, data == null ? null : data.optJSONObject("user"));
        addProfileCandidate(candidates, data == null ? null : data.optJSONObject("habbo"));
        addProfileCandidate(candidates, nestedPayload == null ? null : nestedPayload.optJSONObject("profile"));
        addProfileCandidate(candidates, nestedPayload);
        addProfileCandidate(candidates, root);

        JSONObject withHistory = null;
        JSONObject withIdentity = null;
        for (JSONObject candidate : candidates) {
            if (withHistory == null && (
                    hasNamedListDeep(candidate, "previousNames")
                    || hasNamedListDeep(candidate, "previousMottos")
                    || hasNamedListDeep(candidate, "previousStyles")
                    || hasNamedListDeep(candidate, "friends")
            )) {
                withHistory = candidate;
            }
            if (withIdentity == null && hasProfileIdentity(candidate)) {
                withIdentity = candidate;
            }
        }

        JSONObject selected = withHistory != null ? withHistory : withIdentity;
        if (selected == null) return null;
        if (withIdentity == null || selected == withIdentity) return selected;

        JSONObject merged = copyJsonObject(selected);
        if (merged == null) return selected;
        fillMissingJsonFields(merged, withIdentity);
        return merged;
    }

    private JSONObject validProfileObject(JSONObject obj) {
        if (obj == null) return null;
        if (obj.has("ok") && !obj.optBoolean("ok", true) && !obj.has("data")) return null;
        JSONObject profile = extractHabbodexProfilePayload(obj);
        return hasProfileIdentity(profile) ? profile : null;
    }

    private JSONObject unwrap(JSONObject obj) {
        if (obj == null) return null;
        if (obj.has("ok") && obj.has("data")) {
            Object data = obj.opt("data");
            return data instanceof JSONObject ? unwrap((JSONObject)data) : obj;
        }
        return obj;
    }
    private JSONObject firstObject(JSONObject... objects) { for (JSONObject o : objects) if (o != null && o.length() > 0) return o; return null; }
    private JSONObject firstFromList(JSONObject obj) { ArrayList<JSONObject> list = extractList(obj, null); return list.isEmpty() ? null : list.get(0); }

    private ArrayList<JSONObject> extractSuggestionUsers(JSONObject suggest) {
        ArrayList<JSONObject> users = extractList(suggest, "habbos");
        if (users.isEmpty()) users = extractList(suggest, "users");
        if (users.isEmpty()) users = extractList(suggest, "profiles");
        if (users.isEmpty()) users = extractList(suggest, null);
        if (users.isEmpty()) {
            JSONObject single = validProfileObject(suggest);
            if (single != null) users.add(single);
        }
        return users;
    }

    private ArrayList<JSONObject> extractPreviousNamesFromUser(JSONObject user) {
        ArrayList<JSONObject> out = new ArrayList<>();
        if (user == null) return out;
        out = extractList(user, "previousNames");
        if (!out.isEmpty() || hasNamedListDeep(user, "previousNames")) return out;
        JSONArray values = user.optJSONArray("previousNames");
        JSONObject data = user.optJSONObject("data");
        if (values == null && data != null) values = data.optJSONArray("previousNames");
        if (values == null) return out;
        for (int i = 0; i < values.length(); i++) {
            Object value = values.opt(i);
            if (value instanceof JSONObject) {
                JSONObject item = (JSONObject)value;
                String oldName = firstText(item, "name", "oldName", "username");
                if (!oldName.isEmpty() && firstText(item, "name").isEmpty()) {
                    try { item.put("name", oldName); } catch(Exception ignored) {}
                }
                out.add(item);
            } else if (value != null && value != JSONObject.NULL) {
                String oldName = String.valueOf(value).trim();
                if (!oldName.isEmpty() && !"null".equalsIgnoreCase(oldName)) {
                    try {
                        JSONObject item = new JSONObject();
                        item.put("name", oldName);
                        out.add(item);
                    } catch(Exception ignored) {}
                }
            }
        }
        return out;
    }

    private ArrayList<JSONObject> extractPreviousNamesFromSuggest(JSONObject suggest, String currentName) {
        ArrayList<JSONObject> out = new ArrayList<>();
        String low = normalizeNickKey(currentName);
        for (JSONObject user : extractSuggestionUsers(suggest)) {
            String uname = normalizeNickKey(firstText(user, "name", "username", "habboName"));
            if (!low.isEmpty() && !uname.isEmpty() && !uname.equals(low)) continue;
            out = mergeLists(out, extractPreviousNamesFromUser(user));
        }
        return out;
    }

    private ArrayList<JSONObject> extractList(JSONObject data, String primaryKey) {
        ArrayList<JSONObject> out = new ArrayList<>();
        if (data == null) return out;
        JSONArray arr = findListArrayDeep(data, primaryKey, 0);
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                Object value = arr.opt(i);
                JSONObject item = value instanceof JSONObject
                        ? (JSONObject)value
                        : historicalScalarItem(value, primaryKey);
                if (item != null) out.add(normalizeHistoricalItem(item, primaryKey));
            }
        }
        return out;
    }

    private String[] namedListKeys(String primaryKey) {
        if (isHistoricalKey(primaryKey, "previousNames", "names", "oldNames")) {
            return new String[]{
                    "previousNames", "previous_names", "names",
                    "oldNames", "old_names", "nameHistory", "name_history"
            };
        }
        if (isHistoricalKey(primaryKey, "previousMottos", "mottos", "missions")) {
            return new String[]{
                    "previousMottos", "previous_mottos", "mottos", "missions",
                    "previousMissions", "previous_missions", "mottoHistory", "motto_history"
            };
        }
        if (isHistoricalKey(primaryKey, "previousStyles", "styles", "looks")) {
            return new String[]{
                    "previousStyles", "previous_styles", "styles", "looks",
                    "previousLooks", "previous_looks", "lookHistory", "look_history"
            };
        }
        if (isHistoricalKey(primaryKey, "previousFriends", "removedFriends")) {
            return new String[]{
                    "previousFriends", "previous_friends", "removedFriends",
                    "removed_friends", "oldFriends", "old_friends"
            };
        }
        if (primaryKey == null || primaryKey.trim().isEmpty()) return new String[0];
        return new String[]{primaryKey};
    }

    private JSONArray findNamedListAtLevel(JSONObject object, String primaryKey) {
        if (object == null) return null;
        for (String key : namedListKeys(primaryKey)) {
            JSONArray value = object.optJSONArray(key);
            if (value != null) return value;
        }
        return null;
    }

    private JSONArray findNamedListDeep(
            JSONObject object,
            String primaryKey,
            int depth
    ) {
        if (object == null || depth > 4) return null;
        JSONArray direct = findNamedListAtLevel(object, primaryKey);
        if (direct != null) return direct;

        String[] wrappers = new String[]{"data", "payload", "profile", "user", "habbo"};
        for (String wrapper : wrappers) {
            JSONObject child = object.optJSONObject(wrapper);
            JSONArray nested = findNamedListDeep(child, primaryKey, depth + 1);
            if (nested != null) return nested;
        }
        return null;
    }

    private boolean hasNamedListDeep(JSONObject object, String primaryKey) {
        return findNamedListDeep(object, primaryKey, 0) != null;
    }

    private JSONArray findListArrayDeep(
            JSONObject object,
            String primaryKey,
            int depth
    ) {
        if (object == null || depth > 4) return null;
        JSONArray direct = findNamedListAtLevel(object, primaryKey);
        if (direct != null) return direct;

        String[] genericKeys = new String[]{
                "result", "results", "data", "items", "history", "list",
                "habbos", "users", "profiles"
        };
        for (String key : genericKeys) {
            JSONArray value = object.optJSONArray(key);
            if (value != null) return value;
        }

        String[] wrappers = new String[]{"data", "payload", "profile", "user", "habbo"};
        for (String wrapper : wrappers) {
            JSONObject child = object.optJSONObject(wrapper);
            JSONArray nested = findListArrayDeep(child, primaryKey, depth + 1);
            if (nested != null) return nested;
        }
        return null;
    }

    private JSONArray firstJsonArray(JSONObject object, String... keys) {
        if (object == null || keys == null) return null;
        for (String key : keys) {
            JSONArray value = object.optJSONArray(key);
            if (value != null) return value;
        }
        return null;
    }

    private boolean isHistoricalKey(String key, String... candidates) {
        if (key == null || candidates == null) return false;
        for (String candidate : candidates) if (candidate.equalsIgnoreCase(key)) return true;
        return false;
    }

    private JSONObject historicalScalarItem(Object value, String primaryKey) {
        if (value == null || value == JSONObject.NULL) return null;
        String text = String.valueOf(value).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) return null;
        try {
            JSONObject item = new JSONObject();
            if (isHistoricalKey(primaryKey, "previousNames", "names", "oldNames")) {
                item.put("name", text);
            } else if (isHistoricalKey(primaryKey, "previousMottos", "mottos", "missions")) {
                item.put("text", text);
            } else if (isHistoricalKey(primaryKey, "previousStyles", "styles", "looks")) {
                item.put("figureString", text);
            } else {
                return null;
            }
            return item;
        } catch(Exception ignored) {
            return null;
        }
    }

    private JSONObject normalizeHistoricalItem(JSONObject item, String primaryKey) {
        if (item == null) return null;
        try {
            // Algumas rotas do HabboDex mantêm a identidade em user/habbo e
            // deixam a data no objeto externo. Achata apenas os campos usados
            // pelo aplicativo para que amigos e emblemas oficiais recebam datas.
            JSONObject identity = item.optJSONObject("user");
            if (identity == null) identity = item.optJSONObject("habbo");
            if (identity == null) identity = item.optJSONObject("profile");
            if (identity == null) identity = item.optJSONObject("friend");
            if (identity != null) {
                putIfMissing(item, "uniqueId", firstText(
                        identity, "uniqueId", "habboUniqueId", "id", "habboId", "userId"
                ));
                putIfMissing(item, "name", firstText(
                        identity, "name", "username", "habboName", "nickname"
                ));
                putIfMissing(item, "figureString", firstText(
                        identity, "figureString", "figure", "figure_string", "look"
                ));
            }

            JSONObject badge = item.optJSONObject("badge");
            if (badge != null) {
                putIfMissing(item, "badgeCode", firstText(
                        badge, "badgeCode", "code", "id"
                ));
                putIfMissing(item, "code", firstText(
                        badge, "code", "badgeCode", "id"
                ));
                putIfMissing(item, "name", firstText(
                        badge, "name", "title"
                ));
                putIfMissing(item, "description", firstText(
                        badge, "description", "desc"
                ));
            }

            if (isHistoricalKey(primaryKey, "previousNames", "names", "oldNames")) {
                putIfMissing(item, "name", firstText(
                        item, "oldName", "old_name", "previousName", "previous_name",
                        "username", "nickname", "value"
                ));
            } else if (isHistoricalKey(primaryKey, "previousMottos", "mottos", "missions")) {
                putIfMissing(item, "text", firstText(
                        item, "motto", "mission", "previousMotto", "previous_motto", "value"
                ));
            } else if (isHistoricalKey(primaryKey, "previousStyles", "styles", "looks")) {
                putIfMissing(item, "figureString", firstText(
                        item, "figure", "figure_string", "look", "previousFigure",
                        "previous_figure", "previousLook", "previous_look", "value"
                ));
            }

            String id = habboUniqueIdFromRecord(item);
            putIfMissing(item, "uniqueId", id);
            String figure = firstText(item, "figureString", "figure", "figure_string", "look");
            putIfMissing(item, "figureString", figure);

            String date = firstText(
                    item,
                    "changedAt", "removedAt", "leftAt", "obtainedAt", "acquiredAt",
                    "receivedAt", "creationTime", "friendSince", "addedAt",
                    "createdAt", "date", "timestamp", "datetime",
                    "changed_at", "removed_at", "created_at", "observedAt", "updatedAt",
                    "since", "friendshipSince", "friend_since", "detectedAt",
                    "detected_at", "detectionDate", "firstSeenAt", "first_seen_at"
            );
            if (isHistoricalKey(primaryKey, "previousNames", "names", "oldNames",
                    "previousMottos", "mottos", "missions", "previousStyles", "styles", "looks")) {
                putIfMissing(item, "changedAt", date);
            } else if (isHistoricalKey(primaryKey, "previousFriends", "removedFriends")) {
                putIfMissing(item, "removedAt", date);
            } else if (isHistoricalKey(primaryKey, "friends")) {
                putIfMissing(item, "creationTime", date);
                putIfMissing(item, "friendSince", date);
            } else if (isHistoricalKey(primaryKey, "badges")) {
                putIfMissing(item, "obtainedAt", date);
            }
            putIfMissing(item, "date", date);
        } catch(Exception ignored) {}
        return item;
    }

    private void putIfMissing(JSONObject object, String key, String value) {
        if (object == null || key == null || value == null || value.trim().isEmpty()) return;
        Object current = object.opt(key);
        if (isMissingJsonValue(current)) {
            try { object.put(key, value.trim()); } catch(Exception ignored) {}
        }
    }
    private ArrayList<JSONObject> mergeLists(ArrayList<JSONObject> a, ArrayList<JSONObject> b) { ArrayList<JSONObject> out = new ArrayList<>(); HashSet<String> seen = new HashSet<>(); if (a != null) addUnique(out, seen, a); if (b != null) addUnique(out, seen, b); return out; }
    private void addUnique(ArrayList<JSONObject> out, HashSet<String> seen, ArrayList<JSONObject> src) { for (JSONObject o : src) { String key = stableItemKey(o); if (seen.add(key)) out.add(o); } }
    private String stableItemKey(JSONObject o) {
        if (o == null) return String.valueOf(System.identityHashCode(o));
        String id = normalizeNickKey(habboUniqueIdFromRecord(o));
        if (!id.isEmpty()) return "id:" + id;
        String badge = normalizeNickKey(firstText(o, "badgeCode", "code"));
        if (!badge.isEmpty()) return "badge:" + badge;
        String figure = normalizeNickKey(firstText(o, "figureString", "figure"));
        String when = firstText(o, "changedAt", "date", "createdAt", "creationTime", "time");
        if (!figure.isEmpty() || !when.isEmpty()) return "fig:" + figure + "|" + when;
        String name = normalizeNickKey(firstText(
                o, "name", "username", "habboName", "motto", "text", "mission"
        ));
        if (!name.isEmpty() || !when.isEmpty()) return "txt:" + name + "|" + when;
        return String.valueOf(o.toString().hashCode());
    }
    private String firstText(JSONObject o, String... keys) { if (o == null) return ""; for (String k : keys) { Object v = o.opt(k); if (v == null || v == JSONObject.NULL) continue; String s = String.valueOf(v).trim(); if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) return s; } return ""; }
    private boolean optBoolAny(JSONObject o, boolean fallback, String... keys) { if (o == null) return fallback; for (String k : keys) if (o.has(k)) return o.optBoolean(k, fallback); return fallback; }

    private Boolean optBoolNullable(JSONObject o, String... keys) {
        if (o == null) return null;
        for (String k : keys) {
            if (!o.has(k)) continue;
            Object v = o.opt(k);
            if (v == null || v == JSONObject.NULL) continue;
            if (v instanceof Boolean) return (Boolean)v;
            String s = String.valueOf(v).trim().toLowerCase(Locale.ROOT);
            if (s.equals("true") || s.equals("1") || s.equals("yes")) return true;
            if (s.equals("false") || s.equals("0") || s.equals("no")) return false;
        }
        return null;
    }

    private JSONObject nestedObject(JSONObject o, String key) {
        if (o == null || key == null || key.isEmpty()) return null;
        Object v = o.opt(key);
        return v instanceof JSONObject ? (JSONObject)v : null;
    }

    private Boolean optBoolNullableDeep(JSONObject o, String... keys) {
        if (o == null) return null;
        Boolean direct = optBoolNullable(o, keys);
        if (direct != null) return direct;
        String[] nested = new String[]{"data", "user", "profile", "habbo"};
        for (String n : nested) {
            JSONObject child = nestedObject(o, n);
            if (child == null || child == o) continue;
            Boolean value = optBoolNullable(child, keys);
            if (value != null) return value;
        }
        return null;
    }

    private Boolean explicitProfileVisibility(JSONObject... sources) {
        if (sources == null) return null;
        for (JSONObject source : sources) {
            if (source == null) continue;
            Boolean visible = optBoolNullableDeep(source, "profileVisible", "isProfileVisible", "visible");
            if (visible != null) return visible;
            Boolean privateProfile = optBoolNullableDeep(source, "privateProfile", "profilePrivate", "isPrivate", "isProfilePrivate", "private");
            if (privateProfile != null) return !privateProfile;
        }
        return null;
    }

    private boolean resolveProfilePrivate(
            JSONObject officialPrimary,
            JSONObject officialSecondary,
            JSONObject complementPrimary,
            JSONObject complementSecondary
    ) {
        Boolean officialVisibility = explicitProfileVisibility(officialPrimary, officialSecondary);
        if (officialVisibility != null) return !officialVisibility;
        Boolean complementVisibility = explicitProfileVisibility(complementPrimary, complementSecondary);
        return complementVisibility != null && !complementVisibility;
    }

    private void normalizeProfileState(ProfileResult r) {
        if (r == null) return;
        if (r.banned) {
            r.online = false;
            r.privateProfile = false;
            return;
        }
        r.privateProfile = resolveProfilePrivate(
                r.habboPublic,
                r.officialProfile,
                r.dexProfile,
                r.dex
        );
    }

    private String avatarFull(String figure) { return avatarFull(figure, 2); }
    private String avatarFull(String figure, int direction) { return habboImagingUrl("/habbo-imaging/avatarimage?figure=" + enc(figure) + "&size=l&direction=" + direction + "&head_direction=" + direction + "&gesture=std&action=std&headonly=0"); }
    private String avatarSmall(String figure) { return habboImagingUrl("/habbo-imaging/avatarimage?figure=" + enc(figure) + "&size=m&direction=2&head_direction=2&gesture=std&action=std&headonly=0"); }
    private String avatarHead(String figure) { return habboImagingUrl("/habbo-imaging/avatarimage?figure=" + enc(figure) + "&size=m&direction=2&head_direction=2&headonly=1"); }
    private String avatarHeadByName(String name) { return avatarHeadByNameForHotel(name, currentHotelKey); }
    private String avatarHeadByNameForHotel(String name, String hotelKey) { return "https://" + hotelDomain(hotelKey) + "/habbo-imaging/avatarimage?user=" + enc(name) + "&size=m&direction=2&head_direction=2&headonly=1"; }

    private Drawable makeBg() {
        if (lightTheme) return new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(250, 250, 252), Color.rgb(244, 242, 248), Color.rgb(249, 249, 251)}
        );
        return new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(12, 10, 18), Color.rgb(8, 9, 14), Color.rgb(15, 11, 22)}
        );
    }
    private LinearLayout card(int radius) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        int stroke = currentProfilePrivate ? Color.argb(112, 211, 47, 47) : (lightTheme ? Color.rgb(216, 216, 216) : cardStroke);
        int fill = lightTheme ? Color.rgb(255,255,255) : cardFill;
        l.setBackground(round(fill, radius, stroke, 1));
        if (Build.VERSION.SDK_INT >= 21) l.setElevation(dp(2));
        return l;
    }

    private LinearLayout neutralCard(int radius) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        int stroke = lightTheme ? Color.rgb(216, 216, 216) : cardStroke;
        int fill = lightTheme ? Color.rgb(255,255,255) : cardFill;
        l.setBackground(round(fill, radius, stroke, 1));
        if (Build.VERSION.SDK_INT >= 21) l.setElevation(dp(2));
        return l;
    }

    private void applyProfilePrivateBorder(LinearLayout view, int radius) {
        if (currentProfilePrivate && view != null) {
            view.setBackground(round(lightTheme ? Color.WHITE : cardFill, radius, Color.argb(112, 211, 47, 47), 1));
        }
    }
    private int themeTextColor(int color) {
        if (!lightTheme) return color;
        if (Color.alpha(color) < 255) {
            return Color.rgb(95, 95, 95);
        }
        if (color == Color.WHITE || (Color.red(color) > 180 && Color.green(color) > 180 && Color.blue(color) > 180)) {
            return Color.rgb(33, 33, 33);
        }
        return color;
    }
    private int themeMutedColor() { return lightTheme ? Color.rgb(97, 97, 97) : muted; }
    private TextView text(String s, int sp, int color, boolean bold) { TextView v = new TextView(this); v.setText(s == null ? "" : s); v.setTextSize(sp); v.setTextColor(themeTextColor(color)); if (bold) v.setTypeface(Typeface.DEFAULT_BOLD); return v; }
    private TextView habboText(String s, int sp, boolean bold) { TextView v = text(s, sp, lightTheme ? Color.rgb(33, 33, 33) : Color.WHITE, bold); v.setTypeface(habboFont); return v; }
    private TextView toxicLogoText(String s, int sp) {
        TextView v = habboText(s, sp, true);
        v.setTextColor(lightTheme ? Color.rgb(151, 38, 220) : Color.rgb(238, 104, 255));
        v.setShadowLayer(lightTheme ? dp(1) : dp(4), 0, lightTheme ? dp(1) : dp(2), lightTheme ? Color.argb(80,120,40,170) : Color.rgb(103, 26, 180));
        v.setIncludeFontPadding(false);
        v.setLetterSpacing(0.02f);
        return v;
    }
    private TextView pill(String s, int color) { TextView v = text(s, 13, Color.WHITE, true); v.setGravity(Gravity.CENTER); v.setPadding(dp(14), dp(9), dp(14), dp(9)); v.setBackground(round(adjustAlpha(color, 0.32f), dp(999), adjustAlpha(color,0.55f), 1)); return v; }
    private GradientDrawable round(int fill, int radius, int stroke, int sw) { GradientDrawable d = new GradientDrawable(); d.setColor(fill); d.setCornerRadius(radius); if (sw > 0) d.setStroke(dp(sw), stroke); return d; }
    private GradientDrawable grad(int radius, int c1, int c2) { GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{c1,c2}); d.setCornerRadius(radius); return d; }
    private LinearLayout.LayoutParams lp(int w, int h, int l, int t, int r, int b) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w,h); p.setMargins(dp(l),dp(t),dp(r),dp(b)); return p; }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private int adjustAlpha(int color, float f) { return Color.argb(Math.round(Color.alpha(color)*f), Color.red(color), Color.green(color), Color.blue(color)); }
    private String enc(String s) { try { return URLEncoder.encode(s == null ? "" : s, "UTF-8"); } catch(Exception e){ return s; } }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
    private void hideKeyboard(){ try{ ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchInput.getWindowToken(),0);}catch(Exception ignored){} }
    private void clearSearchFocus(){
        try {
            setSuggestionsVisible(false);
            if (searchInput != null) {
                searchInput.clearFocus();
                searchInput.setCursorVisible(false);
                hideKeyboard();
            }
        } catch(Exception ignored) {}
    }
    private boolean isTouchInsideView(View view, MotionEvent event) {
        if (view == null || event == null) return false;
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        float x = event.getRawX();
        float y = event.getRawY();
        return x >= loc[0] && x <= loc[0] + view.getWidth() && y >= loc[1] && y <= loc[1] + view.getHeight();
    }
    private void openUrl(String url){ try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch(Exception ignored){} }
    private String normalizeUrl(String url) { String s = url == null ? "" : url.trim(); if (s.startsWith("//")) return "https:" + s; if (s.startsWith("/")) return "https://atoxic.com.br" + s; return s; }

    private String emptyDash(String s) { return s == null || s.trim().isEmpty() ? "" : s.trim(); }

    private Date parseHabboDate(String in) {
        if (in == null || in.trim().isEmpty()) return null;
        String s = in.trim();
        try {
            if (s.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                SimpleDateFormat only = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                only.setLenient(false);
                // Datas sem horário representam o dia civil informado pela API.
                // Tratá-las como UTC poderia recuar um dia em fusos negativos.
                only.setTimeZone(TimeZone.getDefault());
                return only.parse(s);
            }
            if (s.matches("^\\d{10,13}$")) {
                long ts = Long.parseLong(s);
                if (s.length() == 10) ts *= 1000;
                return new Date(ts);
            }
            String iso = s.replace("Z", "+0000").replaceAll("([+-]\\d{2}):(\\d{2})$", "$1$2");
            // O HabboDex pode devolver microssegundos (seis casas), enquanto
            // SimpleDateFormat aceita somente milissegundos. Preserva as três
            // primeiras casas antes de interpretar a data.
            iso = iso.replaceFirst("(\\.\\d{3})\\d+(?=([+-]\\d{4})?$)", "$1");
            String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};
            for (String pattern : patterns) {
                try {
                    SimpleDateFormat f = new SimpleDateFormat(pattern, Locale.US);
                    f.setLenient(false);
                    f.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return f.parse(iso);
                } catch(Exception ignored) {}
            }
            boolean usOrder = "com".equals(normalizeHotelKey(currentHotelKey));
            String[] numericPatterns = usOrder
                    ? new String[] {"MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy HH:mm", "MM/dd/yyyy", "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm", "dd/MM/yyyy"}
                    : new String[] {"dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm", "dd/MM/yyyy", "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy HH:mm", "MM/dd/yyyy"};
            for (String pattern : numericPatterns) {
                try {
                    SimpleDateFormat f = new SimpleDateFormat(pattern, Locale.US);
                    f.setLenient(false);
                    f.setTimeZone(TimeZone.getDefault());
                    return f.parse(s);
                } catch(Exception ignored) {}
            }
        } catch(Exception ignored) {}
        return null;
    }

    private String niceDate(String in) {
        if (in == null || in.trim().isEmpty() || "null".equalsIgnoreCase(in.trim())) return "";
        Date d = parseHabboDate(in);
        if (d == null) return in;
        String pattern = "com".equals(normalizeHotelKey(currentHotelKey)) ? "MM/dd/yyyy HH:mm" : "dd/MM/yyyy HH:mm";
        return new SimpleDateFormat(pattern, Locale.ROOT).format(d);
    }

    private String niceDateOnly(String in) {
        if (in == null || in.trim().isEmpty() || "null".equalsIgnoreCase(in.trim())) return "";
        Date d = parseHabboDate(in);
        if (d == null) return in;
        String pattern = "com".equals(normalizeHotelKey(currentHotelKey))
                ? "MM/dd/yyyy"
                : "dd/MM/yyyy";
        return new SimpleDateFormat(pattern, Locale.ROOT).format(d);
    }

    private String timeAgoText(String in) {
        Date d = parseHabboDate(in);
        if (d == null) return "";
        long diff = Math.max(0L, System.currentTimeMillis() - d.getTime()) / 1000L;
        long value;
        int unitResource;
        if (diff < 60) { value = Math.max(1, diff); unitResource = value == 1 ? R.string.ago_second : R.string.ago_seconds; }
        else if (diff < 3600) { value = diff / 60; unitResource = value == 1 ? R.string.ago_minute : R.string.ago_minutes; }
        else if (diff < 86400) { value = diff / 3600; unitResource = value == 1 ? R.string.ago_hour : R.string.ago_hours; }
        else if (diff < 604800) { value = diff / 86400; unitResource = value == 1 ? R.string.ago_day : R.string.ago_days; }
        else if (diff < 2629800) { value = diff / 604800; unitResource = value == 1 ? R.string.ago_week : R.string.ago_weeks; }
        else if (diff < 31557600) { value = diff / 2629800; unitResource = value == 1 ? R.string.ago_month : R.string.ago_months; }
        else { value = diff / 31557600; unitResource = value == 1 ? R.string.ago_year : R.string.ago_years; }
        return tr(R.string.time_ago, value, t(unitResource));
    }

    private boolean isToday(String in) {
        Date value = parseHabboDate(in);
        if (value == null) return false;
        Calendar now = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTime(value);
        return now.get(Calendar.ERA) == date.get(Calendar.ERA)
                && now.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }


    private String findImageUrlDeep(Object obj) {
        HashSet<Object> seen = new HashSet<>();
        return findImageUrlDeep(obj, seen);
    }

    private String findImageUrlDeep(Object obj, HashSet<Object> seen) {
        if (obj == null || obj == JSONObject.NULL || seen.contains(obj)) return "";
        seen.add(obj);
        if (obj instanceof String) {
            String s = ((String)obj).trim();
            if (s.startsWith("http") && (s.matches("(?i).*\\.(png|jpg|jpeg|gif|webp)(\\?.*)?$") || s.contains("habbo") || s.contains("habbodex"))) return s;
            return "";
        }
        if (obj instanceof JSONObject) {
            JSONObject jo = (JSONObject)obj;
            String[] priority = {"url","previewUrl","imageUrl","photoUrl","largeUrl","smallUrl","thumbnailUrl","thumbnail","image","photo","roomImage","badgeUrl"};
            for (String k : priority) {
                Object v = jo.opt(k);
                String found = findImageUrlDeep(v, seen);
                if (!found.isEmpty()) return found;
            }
            Iterator<String> it = jo.keys();
            while (it.hasNext()) {
                String found = findImageUrlDeep(jo.opt(it.next()), seen);
                if (!found.isEmpty()) return found;
            }
        }
        if (obj instanceof JSONArray) {
            JSONArray a = (JSONArray)obj;
            for (int i=0;i<a.length();i++) {
                String found = findImageUrlDeep(a.opt(i), seen);
                if (!found.isEmpty()) return found;
            }
        }
        return "";
    }

    private static class ProfileNotFoundException extends Exception {
        final String nick; final ArrayList<JSONObject> suggestions;
        ProfileNotFoundException(String nick, ArrayList<JSONObject> suggestions) { super("not_found"); this.nick = nick; this.suggestions = suggestions == null ? new ArrayList<>() : suggestions; }
    }


    private void clearLegacyApiProfileCache() {
        try {
            File legacy = new File(getFilesDir(), "profile_cache");
            deleteContents(legacy, true);
        } catch(Exception ignored) {}
    }

    private long cacheDirSize(File dir) {
        long total = 0;
        File[] files = dir == null ? null : dir.listFiles();
        if (files == null) return 0;
        for (File f : files) {
            if (f.isDirectory()) total += cacheDirSize(f);
            else total += Math.max(0, f.length());
        }
        return total;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return tr(R.string.bytes_format, bytes);
        double kb = bytes / 1024.0;
        if (kb < 1024) return tr(R.string.kilobytes_format, kb);
        double mb = kb / 1024.0;
        return tr(R.string.megabytes_format, mb);
    }

    private long fileSize(File file) {
        try { return file != null && file.isFile() ? Math.max(0L, file.length()) : 0L; } catch(Exception ignored) { return 0L; }
    }

    private ArrayList<File> clearableCacheDirs() {
        ArrayList<File> dirs = new ArrayList<>();
        addClearableCacheDir(dirs, getCacheDir());
        try { addClearableCacheDir(dirs, getExternalCacheDir()); } catch(Exception ignored) {}
        return dirs;
    }

    private ArrayList<File> clearableCacheFiles() {
        ArrayList<File> files = new ArrayList<>();
        addClearableCacheFile(files, visualFigureDataDiskCacheFile());
        return files;
    }

    private void addClearableCacheDir(ArrayList<File> dirs, File dir) {
        if (dir == null) return;
        try {
            File canonical = dir.getCanonicalFile();
            for (File existing : dirs) {
                if (existing == null) continue;
                File ec = existing.getCanonicalFile();
                String e = ec.getPath();
                String c = canonical.getPath();
                if (c.equals(e) || c.startsWith(e + File.separator)) return;
            }
            dirs.add(canonical);
        } catch(Exception ignored) {
            if (!dirs.contains(dir)) dirs.add(dir);
        }
    }

    private void addClearableCacheFile(ArrayList<File> files, File file) {
        if (file == null) return;
        try {
            File canonical = file.getCanonicalFile();
            if (!files.contains(canonical)) files.add(canonical);
        } catch(Exception ignored) {
            if (!files.contains(file)) files.add(file);
        }
    }

    private boolean isInsideAnyDir(File file, ArrayList<File> dirs) {
        if (file == null || dirs == null) return false;
        try {
            String f = file.getCanonicalPath();
            for (File dir : dirs) {
                if (dir == null) continue;
                String d = dir.getCanonicalPath();
                if (f.equals(d) || f.startsWith(d + File.separator)) return true;
            }
        } catch(Exception ignored) {}
        return false;
    }

    private long clearableCacheBytes() {
        long total = 0L;
        ArrayList<File> dirs = clearableCacheDirs();
        for (File dir : dirs) total += cacheDirSize(dir);
        for (File file : clearableCacheFiles()) {
            if (!isInsideAnyDir(file, dirs)) total += fileSize(file);
        }
        return total;
    }

    private String cacheStatsText() {
        return t(R.string.app_cache) + ": " + formatBytes(clearableCacheBytes());
    }

    private void updateCacheStatsLabelAsync(final TextView info) {
        if (info == null) return;
        executor.execute(() -> {
            final String txt = cacheStatsText();
            runOnUiThread(() -> {
                try { info.setText(txt); } catch(Exception ignored) {}
            });
        });
    }

    private void rebuildUiPreservingProfile() {
        ProfileResult keep = activeRenderedProfile;
        buildUi();
        if (keep != null) renderProfile(keep);
        refreshSponsors();
    }

    private void clearProfileCache() {
        clearProfileCache(null);
    }

    private void clearProfileCache(Runnable done) {
        visualFigureDataCache = null;
        visualFigureDataLoadedAt = 0L;
        visualEditorCachedFigure = DEFAULT_VISUAL_FIGURE;
        visualEditorCachedGender = "M";
        visualEditorCachedType = "hd";
        visualEditorCachedDirection = 2;
        visualItemViewsSessionCache.clear();
        visualItemRenderLimits.clear();
        jsonResponseCache.clear();
        try {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .remove(PREF_VISUAL_EDITOR_FIGURE)
                .remove(PREF_VISUAL_EDITOR_GENDER)
                .remove(PREF_VISUAL_EDITOR_TYPE)
                .remove(PREF_VISUAL_EDITOR_DIRECTION)
                .remove(PREF_FAVORITE_ONLINE_STATES)
                .apply();
        } catch(Exception ignored) {}

        // Evita travar a UI ao limpar cache; o cache em disco do Glide é removido abaixo em thread separada.

        executor.execute(() -> {
            try { Glide.get(MainActivity.this).clearDiskCache(); } catch (Exception ignored) {}

            // Usa a mesma lista usada em clearableCacheBytes(), para o tamanho mostrado
            // em Configurações bater com o que o botão realmente remove.
            ArrayList<File> dirs = clearableCacheDirs();
            for (File dir : dirs) {
                deleteContents(dir, false);
            }
            for (File file : clearableCacheFiles()) {
                if (!isInsideAnyDir(file, dirs)) {
                    try { if (file != null && file.exists()) file.delete(); } catch(Exception ignored) {}
                }
            }

            // Não apagamos codeCacheDir aqui para evitar travamentos/descompilações desnecessárias.

            runOnUiThread(() -> {
                if (done != null) done.run();
            });
        });
    }

    private void deleteContents(File dir, boolean deleteRoot) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f == null) continue;
                if (f.isDirectory()) deleteContents(f, true);
                else { try { f.delete(); } catch(Exception ignored) {} }
            }
        }
        if (deleteRoot) { try { dir.delete(); } catch(Exception ignored) {} }
    }

    private int dialogFillColor() { return lightTheme ? Color.rgb(255,255,255) : Color.rgb(20, 18, 28); }
    private int dialogStrokeColor() { return lightTheme ? Color.rgb(216,216,216) : Color.rgb(58, 52, 73); }

    private void loadOpenedProfilesHistory() {
        openedProfilesHistory.clear();
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_OPENED_HISTORY, "");
        if (raw == null || raw.trim().isEmpty()) return;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length() && openedProfilesHistory.size() < 50; i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String nick = o.optString("nick", "").trim();
                if (nick.isEmpty()) continue;
                String hotel = normalizeHotelKey(o.optString("hotel", "br"));
                if (hotel.isEmpty()) hotel = "br";
                openedProfilesHistory.add(new ProfileHistoryItem(nick, o.optString("figure", ""), hotel, o.optString("uniqueId", o.optString("id", ""))));
            }
        } catch(Exception ignored) {}
    }

    private void saveOpenedProfilesHistory() {
        JSONArray arr = new JSONArray();
        try {
            for (ProfileHistoryItem item : openedProfilesHistory) {
                JSONObject o = new JSONObject();
                o.put("nick", item.nick);
                o.put("figure", item.figure);
                o.put("uniqueId", item.uniqueId);
                String hotel = normalizeHotelKey(item.hotelKey);
                o.put("hotel", hotel.isEmpty() ? "br" : hotel);
                arr.put(o);
            }
        } catch(Exception ignored) {}
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_OPENED_HISTORY, arr.toString()).apply();
    }


    private int bottomNavIconColor(boolean selected) {
        if (selected) return lightTheme ? Color.rgb(18,18,18) : Color.WHITE;
        return lightTheme ? Color.rgb(120,120,128) : Color.argb(155,255,255,255);
    }

    private int bottomNavDividerColor() {
        return lightTheme ? Color.rgb(224,224,228) : Color.rgb(44,44,52);
    }

    private Drawable bottomNavBackground() {
        return new BottomNavBarDrawable();
    }

    private FrameLayout addBottomNavigation(FrameLayout host, int selectedTab, Dialog activeDialog) {
        if (host == null) return null;

        applySafeAreaInsets(activeDialog == null ? getWindow() : activeDialog.getWindow(), host);

        FrameLayout navWrap = new FrameLayout(this);
        navWrap.setBackground(bottomNavBackground());
        if (Build.VERSION.SDK_INT >= 21) navWrap.setElevation(dp(18));

        View divider = new View(this);
        divider.setBackgroundColor(bottomNavDividerColor());
        FrameLayout.LayoutParams dividerLp = new FrameLayout.LayoutParams(-1, dp(1), Gravity.TOP);
        dividerLp.leftMargin = dp(22);
        dividerLp.rightMargin = dp(22);
        dividerLp.topMargin = dp(1);
        navWrap.addView(divider, dividerLp);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(7), dp(6), dp(7), dp(6));
        FrameLayout.LayoutParams navInnerLp = new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER);
        navWrap.addView(nav, navInnerLp);

        FrameLayout.LayoutParams navLp = new FrameLayout.LayoutParams(-1, dp(64), Gravity.BOTTOM);
        navLp.leftMargin = dp(12);
        navLp.rightMargin = dp(12);
        navLp.bottomMargin = dp(10);
        host.addView(navWrap, navLp);

        View searchNavItem = bottomNavItem("home", selectedTab == 0, () -> {
            if (activeDialog != null) activeDialog.dismiss();
        });
        if (selectedTab == 0 && activeDialog == null) bindSearchNavigationGestures(searchNavItem);
        nav.addView(searchNavItem, new LinearLayout.LayoutParams(0, -1, 1));

        View visualsNavItem = bottomNavItem("visuals", selectedTab == 1, () -> {
            if (selectedTab == 1) return;
            showVisualEditorDialog();
            if (activeDialog != null) uiHandler.postDelayed(() -> {
                try { activeDialog.dismiss(); } catch (Exception ignored) {}
            }, 120L);
        });
        nav.addView(visualsNavItem, new LinearLayout.LayoutParams(0, -1, 1));

        nav.addView(bottomNavItem("heart", selectedTab == 2, () -> {
            if (selectedTab == 2) return;
            showFavoriteProfilesDialog();
            if (activeDialog != null) uiHandler.postDelayed(() -> {
                try { activeDialog.dismiss(); } catch (Exception ignored) {}
            }, 120L);
        }), new LinearLayout.LayoutParams(0, -1, 1));

        View settingsNavItem = bottomNavItem("settings", selectedTab == 3, () -> {
            if (selectedTab == 3) return;
            showSettingsDialog();
            if (activeDialog != null) uiHandler.postDelayed(() -> {
                try { activeDialog.dismiss(); } catch (Exception ignored) {}
            }, 120L);
        });
        nav.addView(settingsNavItem, new LinearLayout.LayoutParams(0, -1, 1));
        if (selectedTab == 0 && activeDialog == null) {
            mainTutorialVisualsTarget = visualsNavItem;
            mainTutorialSettingsTarget = settingsNavItem;
        }
        return navWrap;
    }

    private View bottomNavItem(String icon, boolean selected, final Runnable action) {
        FrameLayout item = new FrameLayout(this);
        item.setClickable(true);
        item.setFocusable(true);
        item.setBackground(selected
                ? round(
                        lightTheme ? Color.rgb(235, 229, 250) : Color.argb(74, 139, 92, 246),
                        dp(17),
                        lightTheme ? Color.rgb(205, 192, 238) : Color.argb(92, 167, 139, 250),
                        1
                )
                : new ColorDrawable(Color.TRANSPARENT));
        item.setPadding(dp(5), dp(3), dp(5), dp(3));

        TextView iv = text("", 1, bottomNavIconColor(selected), true);
        iv.setGravity(Gravity.CENTER);
        iv.setPadding(0, 0, 0, 0);
        iv.setBackground(new BottomNavIconDrawable(icon, selected));
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(dp(28), dp(28), Gravity.CENTER);
        item.addView(iv, ip);

        if ("heart".equals(icon)) {
            TextView badge = text("", 10, Color.WHITE, true);
            badge.setTextColor(Color.WHITE);
            badge.setGravity(Gravity.CENTER);
            badge.setIncludeFontPadding(false);
            badge.setPadding(dp(4), 0, dp(4), 0);
            int count = favoriteOnlineCount();
            int bw = count >= 10 ? dp(24) : dp(18);
            badge.setMinWidth(bw);
            badge.setBackground(round(lightTheme ? Color.rgb(15, 15, 18) : purple, dp(999), lightTheme ? Color.rgb(255,255,255) : Color.argb(150,0,0,0), 1));
            badge.setText(count > 0 ? String.valueOf(count) : "");
            badge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(bw, dp(18), Gravity.CENTER);
            bp.leftMargin = dp(18);
            bp.topMargin = -dp(12);
            item.addView(badge, bp);
            favoriteOnlineBadgeViews.add(badge);
            updateFavoriteOnlineBadgeText();
        }

        item.setOnClickListener(v -> {
            if (action != null) action.run();
        });
        return item;
    }

    private void scrollMainToTop(boolean focusSearch) {
        if (mainScroll != null) mainScroll.smoothScrollTo(0, 0);
        if (!focusSearch) return;
        uiHandler.postDelayed(() -> {
            if (searchInput == null) return;
            searchInput.requestFocus();
            searchInput.setCursorVisible(true);
            searchInput.setSelection(searchInput.getText().length());
            try {
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
            } catch (Exception ignored) {}
        }, 280L);
    }

    private void bindSearchNavigationGestures(View target) {
        if (target == null) return;
        final Runnable[] holdTask = new Runnable[1];
        final float[] downX = {0f};
        final float[] downY = {0f};
        final long[] lastTapAt = {0L};
        final boolean[] holdTriggered = {false};
        final boolean[] moved = {false};
        target.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                downX[0] = event.getX();
                downY[0] = event.getY();
                holdTriggered[0] = false;
                moved[0] = false;
                holdTask[0] = () -> {
                    holdTriggered[0] = true;
                    try { v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS); } catch (Exception ignored) {}
                    scrollMainToTop(true);
                };
                uiHandler.postDelayed(holdTask[0], 2000L);
                return true;
            }
            if (action == MotionEvent.ACTION_MOVE) {
                if (Math.abs(event.getX() - downX[0]) > dp(12) || Math.abs(event.getY() - downY[0]) > dp(12)) {
                    moved[0] = true;
                    if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                }
                return true;
            }
            if (action == MotionEvent.ACTION_UP) {
                if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                if (!holdTriggered[0] && !moved[0]) {
                    long now = SystemClock.uptimeMillis();
                    if (now - lastTapAt[0] <= 360L) {
                        lastTapAt[0] = 0L;
                        scrollMainToTop(false);
                    } else {
                        lastTapAt[0] = now;
                    }
                }
                return true;
            }
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE) {
                if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                return true;
            }
            return true;
        });
    }

    private void bindBottomNavigationAutoHide(ScrollView scrollSource, View navigation) {
        if (scrollSource == null || navigation == null) return;
        final boolean[] hidden = {false};
        final int[] directionDistance = {0};
        navigation.setTranslationY(0f);
        scrollSource.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int delta = scrollY - oldScrollY;
            if (scrollY <= dp(4)) {
                directionDistance[0] = 0;
                if (hidden[0]) {
                    hidden[0] = false;
                    navigation.animate().cancel();
                    navigation.animate().translationY(0f).setDuration(170L).start();
                }
                return;
            }
            if (delta > 0) {
                directionDistance[0] = Math.max(0, directionDistance[0]) + delta;
                if (!hidden[0] && directionDistance[0] >= dp(12)) {
                    hidden[0] = true;
                    directionDistance[0] = 0;
                    navigation.animate().cancel();
                    navigation.animate()
                            .translationY(Math.max(navigation.getHeight(), dp(64)) + dp(18))
                            .setDuration(180L)
                            .start();
                }
            } else if (delta < 0) {
                directionDistance[0] = Math.min(0, directionDistance[0]) + delta;
                if (hidden[0] && -directionDistance[0] >= dp(8)) {
                    hidden[0] = false;
                    directionDistance[0] = 0;
                    navigation.animate().cancel();
                    navigation.animate().translationY(0f).setDuration(170L).start();
                }
            }
        });
    }




    private View visualPurpleLoader(String message) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(12), dp(14), dp(12), dp(14));
        box.setMinimumHeight(dp(84));
        box.setBackground(round(Color.argb(lightTheme ? 18 : 26, 139, 52, 217), dp(18), Color.argb(70, 139, 52, 217), 1));

        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= 21) {
            spinner.setIndeterminateTintList(ColorStateList.valueOf(Color.rgb(150, 58, 242)));
        }
        box.addView(spinner, new LinearLayout.LayoutParams(dp(34), dp(34)));

        String cleanMessage = message == null ? "" : message.trim();
        if (!cleanMessage.isEmpty()) {
            TextView label = text(cleanMessage, 13, lightTheme ? Color.rgb(70, 36, 92) : Color.WHITE, true);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.topMargin = dp(10);
            box.addView(label, lp);
        }
        return box;
    }

    private View visualFigureDataLoadingView() {
        FrameLayout outer = new FrameLayout(this);
        outer.setPadding(dp(18), dp(18), dp(18), dp(18));
        outer.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(22), dp(24), dp(22), dp(24));
        card.setMinimumHeight(dp(300));

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                lightTheme
                        ? new int[]{Color.rgb(255, 255, 255), Color.rgb(247, 241, 255), Color.rgb(238, 228, 255)}
                        : new int[]{Color.rgb(25, 13, 40), Color.rgb(42, 20, 67), Color.rgb(20, 12, 34)}
        );
        bg.setCornerRadius(dp(24));
        bg.setStroke(dp(1), lightTheme ? Color.rgb(224, 204, 247) : Color.argb(95, 190, 115, 255));
        card.setBackground(bg);
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(8));

        FrameLayout spinnerWrap = new FrameLayout(this);
        GradientDrawable halo = new GradientDrawable();
        halo.setShape(GradientDrawable.OVAL);
        halo.setColor(lightTheme ? Color.rgb(248, 242, 255) : Color.argb(48, 160, 62, 255));
        halo.setStroke(dp(1), lightTheme ? Color.rgb(224, 204, 247) : Color.argb(80, 255, 255, 255));
        spinnerWrap.setBackground(halo);
        if (Build.VERSION.SDK_INT >= 21) spinnerWrap.setElevation(dp(3));

        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= 21) {
            spinner.setIndeterminateTintList(ColorStateList.valueOf(Color.rgb(139, 52, 217)));
        }
        FrameLayout.LayoutParams spLp = new FrameLayout.LayoutParams(dp(42), dp(42), Gravity.CENTER);
        spinnerWrap.addView(spinner, spLp);
        LinearLayout.LayoutParams haloLp = new LinearLayout.LayoutParams(dp(72), dp(72));
        card.addView(spinnerWrap, haloLp);

        TextView title = text(t(R.string.loading_visuals), 16, lightTheme ? Color.rgb(40, 23, 55) : Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        title.setIncludeFontPadding(false);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = dp(16);
        card.addView(title, titleLp);

        TextView sub = text(t(R.string.preparing_clothes_colors), 12, lightTheme ? Color.rgb(110, 86, 130) : Color.argb(180, 255, 255, 255), false);
        sub.setGravity(Gravity.CENTER);
        sub.setIncludeFontPadding(false);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.topMargin = dp(7);
        card.addView(sub, subLp);

        LinearLayout dots = new LinearLayout(this);
        dots.setOrientation(LinearLayout.HORIZONTAL);
        dots.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dotsLp = new LinearLayout.LayoutParams(-1, dp(8));
        dotsLp.topMargin = dp(16);
        for (int i = 0; i < 3; i++) {
            View dot = new View(this);
            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);
            dotBg.setColor(i == 1 ? Color.rgb(139, 52, 217) : Color.argb(lightTheme ? 90 : 130, 139, 52, 217));
            dot.setBackground(dotBg);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(dp(7), dp(7));
            dlp.leftMargin = dp(3);
            dlp.rightMargin = dp(3);
            dots.addView(dot, dlp);
        }
        card.addView(dots, dotsLp);

        outer.addView(card, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));
        return outer;
    }

    private void bindNestedScrollTouch(final ScrollView scroll) {
        if (scroll == null) return;
        final float[] lastY = {0f};
        scroll.setNestedScrollingEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setOnTouchListener((v, event) -> {
            ViewParent parent = v.getParent();
            if (parent != null) parent.requestDisallowInterceptTouchEvent(true);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastY[0] = event.getY();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float y = event.getY();
                int dy = (int)(lastY[0] - y);
                lastY[0] = y;
                scroll.scrollBy(0, dy);
                if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (parent != null) parent.requestDisallowInterceptTouchEvent(false);
                return true;
            }
            return true;
        });
    }

    private void bindColorPanelTouchLock(final View view) {
        if (view == null) return;
        view.setOnTouchListener((v, event) -> {
            ViewParent parent = v.getParent();
            while (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
                if (!(parent instanceof View)) break;
                parent = ((View) parent).getParent();
            }
            return false;
        });
    }

    private void loadVisualEditorState() {
        try {
            SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
            visualEditorCachedFigure = sp.getString(PREF_VISUAL_EDITOR_FIGURE, DEFAULT_VISUAL_FIGURE);
            visualEditorCachedGender = sp.getString(PREF_VISUAL_EDITOR_GENDER, "M");
            visualEditorCachedType = sp.getString(PREF_VISUAL_EDITOR_TYPE, "hd");
            visualEditorCachedDirection = sp.getInt(PREF_VISUAL_EDITOR_DIRECTION, 2);
            if (visualEditorCachedFigure == null || visualEditorCachedFigure.trim().isEmpty()) visualEditorCachedFigure = DEFAULT_VISUAL_FIGURE;
            if (visualEditorCachedGender == null || visualEditorCachedGender.trim().isEmpty()) visualEditorCachedGender = "M";
            if (visualEditorCachedType == null || visualEditorCachedType.trim().isEmpty()) visualEditorCachedType = "hd";
        } catch(Exception ignored) {}
    }

    private void saveVisualEditorState() {
        try {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(PREF_VISUAL_EDITOR_FIGURE, visualEditorCachedFigure == null ? DEFAULT_VISUAL_FIGURE : visualEditorCachedFigure)
                    .putString(PREF_VISUAL_EDITOR_GENDER, visualEditorCachedGender == null ? "M" : visualEditorCachedGender)
                    .putString(PREF_VISUAL_EDITOR_TYPE, visualEditorCachedType == null ? "hd" : visualEditorCachedType)
                    .putInt(PREF_VISUAL_EDITOR_DIRECTION, visualEditorCachedDirection)
                    .apply();
        } catch(Exception ignored) {}
    }

    private String avatarMedium(String figure, int direction) {
        return "https://www.habbo.com.br/habbo-imaging/avatarimage?figure=" + enc(figure) + "&size=m&direction=" + direction + "&head_direction=" + direction + "&gesture=std&action=std";
    }

    private void showVisualEditorDialog() {
        final Dialog dialog = new Dialog(this);
        PullDispatchFrameLayout full = new PullDispatchFrameLayout(this);
        full.setBackground(makeBg());

        ScrollView visualScroll = new ScrollView(this);
        visualScroll.setFillViewport(false);
        visualScroll.setVerticalScrollBarEnabled(true);
        visualScroll.setScrollbarFadingEnabled(false);
        tintScrollBar(visualScroll);
        full.addView(visualScroll, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(16), dp(68), dp(16), dp(88));
        wrap.setBackgroundColor(Color.TRANSPARENT);
        visualScroll.addView(wrap, new ScrollView.LayoutParams(-1, -2));

        bindBottomNavigationAutoHide(
                visualScroll,
                addBottomNavigation(full, 1, dialog)
        );
        dialog.setContentView(full);
        applySafeAreaInsets(dialog.getWindow(), full);

        // Top wardrobe banner. Keep it above the nick/search controls.
        View visualNickBanner = buildVisualNickSearchBannerAd();
        if (visualNickBanner != null) {
            wrap.addView(visualNickBanner, lp(-1, dp(68), 0, 0, 0, 10));
            requestVisualNickSearchBannerLoadIfNeeded();
        }

        LinearLayout nickRow = new LinearLayout(this);
        nickRow.setOrientation(LinearLayout.HORIZONTAL);
        nickRow.setGravity(Gravity.CENTER_VERTICAL);
        EditText nickInput = new EditText(this);
        nickInput.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
        nickInput.setHintTextColor(lightTheme ? Color.rgb(125,125,125) : Color.argb(150,255,255,255));
        nickInput.setTextSize(14);
        nickInput.setTypeface(habboFont);
        nickInput.setSingleLine(true);
        nickInput.setHint(t(R.string.type_nick));
        nickInput.setPadding(dp(12), 0, dp(12), 0);
        nickInput.setBackground(round(lightTheme ? Color.WHITE : Color.argb(18,255,255,255), dp(14), lightTheme ? Color.rgb(218,218,218) : Color.argb(30,255,255,255), 1));
        nickRow.addView(nickInput, new LinearLayout.LayoutParams(0, dp(46), 1));

        TextView loadNick = dialogButton(t(R.string.search_button));
        LinearLayout.LayoutParams loadLp = new LinearLayout.LayoutParams(dp(92), dp(46));
        loadLp.leftMargin = dp(8);
        nickRow.addView(loadNick, loadLp);
        wrap.addView(nickRow, lp(-1, dp(46), 0, 0, 0, 8));

        nickInput.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                loadNick.performClick();
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(nickInput.getWindowToken(), 0);
                } catch(Exception ignored) {}
                nickInput.clearFocus();
                return true;
            }
            return false;
        });

        FrameLayout visualPreviewFrame = new FrameLayout(this);
        visualPreviewFrame.setBackground(round(lightTheme ? Color.rgb(252,252,252) : Color.rgb(15, 8, 25), dp(20), lightTheme ? Color.rgb(222,222,226) : Color.argb(22,255,255,255), 1));
        wrap.addView(visualPreviewFrame, lp(-1, dp(220), 0, 0, 0, 10));

        ImageView preview = new ImageView(this);
        preview.setAdjustViewBounds(true);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setPadding(dp(20), dp(8), dp(20), dp(8));
        visualPreviewFrame.addView(preview, new FrameLayout.LayoutParams(-1, -1));


        final String[] currentFigure = {visualEditorCachedFigure == null || visualEditorCachedFigure.trim().isEmpty() ? DEFAULT_VISUAL_FIGURE : visualEditorCachedFigure};
        final String[] currentGender = {visualEditorCachedGender == null || visualEditorCachedGender.trim().isEmpty() ? "M" : visualEditorCachedGender};
        final String[] currentType = {"hd"};
        final int[] visualDirection = {visualEditorCachedDirection};
        final Runnable[] refreshAll = new Runnable[1];
        final JSONObject[] figureDataRef = {visualFigureDataCache};
        final boolean[] visualInitialContentRendered = {false};
        final boolean[] visualDialogClosed = {false};
        final Runnable[] finishVisualInitialLoadIfPossible = new Runnable[1];
        final Runnable[] visualInitialLoadWatchdog = new Runnable[1];
        dialog.setOnDismissListener(d -> {
            visualDialogClosed[0] = true;
            cancelTutorialPulseAnimation();
            if (visualTutorialOverlayView != null) detachViewFromParent(visualTutorialOverlayView);
            visualItemTutorialScheduled = false;
            visualItemTutorialRunning = false;
            visualItemTutorialTarget = null;
            visualTutorialOverlayView = null;
        });

        TextView saveLookBtn = visualCornerIconButton(new VisualSaveLookDrawable());
        FrameLayout.LayoutParams saveLookLp = new FrameLayout.LayoutParams(dp(28), dp(28), Gravity.TOP | Gravity.LEFT);
        saveLookLp.topMargin = dp(28);
        saveLookLp.leftMargin = dp(15);
        full.addView(saveLookBtn, saveLookLp);
        if (Build.VERSION.SDK_INT >= 21) saveLookBtn.setElevation(dp(18));
        saveLookBtn.setOnClickListener(v -> saveVisualEditorLook(currentFigure[0], currentGender[0]));

        TextView savedLooksBtn = visualCornerIconButton(new VisualSavedLooksDrawable());
        FrameLayout.LayoutParams savedLooksLp = new FrameLayout.LayoutParams(dp(28), dp(28), Gravity.TOP | Gravity.RIGHT);
        savedLooksLp.topMargin = dp(28);
        savedLooksLp.rightMargin = dp(15);
        full.addView(savedLooksBtn, savedLooksLp);
        if (Build.VERSION.SDK_INT >= 21) savedLooksBtn.setElevation(dp(18));
        savedLooksBtn.setOnClickListener(v -> showSavedVisualsDialog(currentFigure, currentGender, currentType, figureDataRef, refreshAll));

        LinearLayout catTabs = new LinearLayout(this);
        catTabs.setOrientation(LinearLayout.VERTICAL);
        catTabs.setPadding(0, 0, 0, 0);
        wrap.addView(catTabs, lp(-1, dp(110), 0, 0, 0, 12));

        ScrollView itemScroll = new ScrollView(this);
        itemScroll.setVerticalScrollBarEnabled(true);
        itemScroll.setScrollbarFadingEnabled(false);
        itemScroll.setNestedScrollingEnabled(true);
        itemScroll.setOnTouchListener((v, event) -> {
            ViewParent parent = v.getParent();
            if (parent != null) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    parent.requestDisallowInterceptTouchEvent(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
            }
            return false;
        });
        tintScrollBar(itemScroll);
        LinearLayout itemsArea = new LinearLayout(this);
        itemsArea.setOrientation(LinearLayout.VERTICAL);
        itemScroll.addView(itemsArea, new ScrollView.LayoutParams(-1, -2));
        itemScroll.setBackground(round(Color.argb(lightTheme ? 18 : 44, 0, 0, 0), dp(20), Color.argb(lightTheme ? 30 : 35, 255,255,255), 1));

        FrameLayout visualItemsHost = new FrameLayout(this);
        visualItemsHost.setBackgroundColor(Color.TRANSPARENT);
        visualItemsHost.addView(itemScroll, new FrameLayout.LayoutParams(-1, -1));
        View visualFigureLoader = visualFigureDataLoadingView();
        visualItemsHost.addView(visualFigureLoader, new FrameLayout.LayoutParams(-1, -1));
        wrap.addView(visualItemsHost, lp(-1, dp(310), 0, 0, 0, 10));

        final Runnable showVisualFigureLoader = () -> {
            itemScroll.setVisibility(View.INVISIBLE);
            visualFigureLoader.setVisibility(View.VISIBLE);
        };
        final Runnable hideVisualFigureLoader = () -> {
            visualFigureLoader.setVisibility(View.GONE);
            itemScroll.setVisibility(View.VISIBLE);
        };

        LinearLayout colorPanel = new LinearLayout(this);
        colorPanel.setOrientation(LinearLayout.VERTICAL);
        colorPanel.setPadding(dp(10), dp(10), dp(10), dp(10));
        colorPanel.setBackground(round(Color.argb(lightTheme ? 24 : 44, 0, 0, 0), dp(20), Color.argb(lightTheme ? 30 : 35, 255,255,255), 1));
        colorPanel.setVisibility(View.GONE);
        bindColorPanelTouchLock(colorPanel);
        wrap.addView(colorPanel, lp(-1, -2, 0, 6, 0, 8));
        View visualColorsBanner = buildVisualColorsBannerAd();
        if (visualColorsBanner != null) {
            wrap.addView(visualColorsBanner, lp(-1, dp(68), 0, 0, 0, 10));
            requestVisualColorsBannerLoadIfNeeded();
        }

        Runnable updatePreview = () -> {
            visualEditorCachedFigure = currentFigure[0];
            visualEditorCachedGender = currentGender[0];
            visualEditorCachedType = currentType[0];
            visualEditorCachedDirection = visualDirection[0];
            saveVisualEditorState();
            loadAvatarImageKeepingCurrent(preview, avatarFull(currentFigure[0], visualDirection[0]));
        };
        updatePreview.run();

        final float[] visualSwipeStartX = {0f};
        final float[] visualSwipeStartY = {0f};
        final boolean[] visualSwipeConsumed = {false};
        preview.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) requestDisallowParents(v, true);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    visualSwipeStartX[0] = event.getX();
                    visualSwipeStartY[0] = event.getY();
                    visualSwipeConsumed[0] = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float moveDx = event.getX() - visualSwipeStartX[0];
                    float moveDy = event.getY() - visualSwipeStartY[0];
                    if (!visualSwipeConsumed[0] && Math.abs(moveDx) >= dp(34) && Math.abs(moveDx) > Math.abs(moveDy)) {
                        if (moveDx > 0) visualDirection[0] = (visualDirection[0] + 1) % 8;
                        else {
                            visualDirection[0] = visualDirection[0] - 1;
                            if (visualDirection[0] < 0) visualDirection[0] = 7;
                        }
                        visualSwipeConsumed[0] = true;
                        visualSwipeStartX[0] = event.getX();
                        visualSwipeStartY[0] = event.getY();
                        updatePreview.run();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    requestDisallowParents(v, false);
                    return true;
            }
            return true;
        });

        refreshAll[0] = () -> {
            currentGender[0] = detectVisualGenderFromFigure(currentFigure[0], figureDataRef[0], currentGender[0]);
            JSONObject currentData = figureDataRef[0];
            String activeItemType = getVisualItemTypeForUiCategory(currentType[0]);
            if (currentData != null && visualCategory(currentData, activeItemType) != null) {
                hideVisualFigureLoader.run();
                colorPanel.setVisibility(View.VISIBLE);
            }
            renderVisualCategories(catTabs, currentType, currentGender, currentFigure, currentData, refreshAll[0]);
            renderVisualItems(itemsArea, colorPanel, currentFigure, currentGender, currentType, currentData, updatePreview);
            updatePreview.run();
            uiHandler.postDelayed(() -> maybeShowVisualItemTutorial(full, visualScroll), 260L);
        };

        finishVisualInitialLoadIfPossible[0] = () -> {
            if (visualInitialContentRendered[0] || visualDialogClosed[0]) return;
            JSONObject availableData = figureDataRef[0] != null ? figureDataRef[0] : visualFigureDataCache;
            if (availableData == null) return;
            String requestedType = getVisualItemTypeForUiCategory(currentType[0]);
            boolean hasRequestedCategory = visualCategory(availableData, requestedType) != null;
            boolean hasAnyCategory = countVisualLoadedCategories(availableData) > 0;
            if (!hasRequestedCategory && !hasAnyCategory) return;
            figureDataRef[0] = availableData;
            visualInitialContentRendered[0] = true;
            hideVisualFigureLoader.run();
            refreshAll[0].run();
        };

        visualInitialLoadWatchdog[0] = () -> {
            if (visualInitialContentRendered[0] || visualDialogClosed[0]) return;
            finishVisualInitialLoadIfPossible[0].run();
            if (!visualInitialContentRendered[0] && !visualDialogClosed[0]) {
                uiHandler.postDelayed(visualInitialLoadWatchdog[0], 250L);
            }
        };

        loadNick.setOnClickListener(v -> {
            try {
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(nickInput.getWindowToken(), 0);
            } catch(Exception ignored) {}
            nickInput.clearFocus();
            String nick = nickInput.getText().toString().trim();
            if (nick.isEmpty()) {
                toast(t(R.string.type_nick_toast));
                return;
            }
            loadNick.setText("...");
            executor.submit(() -> {
                try {
                    JSONObject p = validProfileObject(tryJson(habboApiUrl("/api/public/users?name=" + enc(nick))));
                    String fig = firstText(p, "figureString", "figure");
                    String g = firstText(p, "gender", "sex");
                    if (fig.isEmpty()) {
                        JSONObject d = resolveHabbodexProfileFromSuggestions(
                                fetchHabbodexSuggestions(nick),
                                nick
                        );
                        fig = firstText(d, "figureString", "figure");
                        if (g.isEmpty()) g = firstText(d, "gender", "sex");
                    }
                    final String f = fig;
                    final String gender = g;
                    runOnUiThread(() -> {
                        loadNick.setText(t(R.string.search_button));
                        if (f == null || f.trim().isEmpty()) {
                            toast(t(R.string.not_found_simple));
                            return;
                        }
                        currentFigure[0] = f.trim();
                        currentGender[0] = detectVisualGenderFromFigure(currentFigure[0], figureDataRef[0], normalizeVisualGender(gender, currentGender[0]));
                        visualItemViewsSessionCache.clear();
        visualItemRenderLimits.clear();
                        refreshAll[0].run();
                    });
                } catch(Exception e) {
                    runOnUiThread(() -> {
                        loadNick.setText(t(R.string.search_button));
                        toast(t(R.string.cannot_load_visuals));
                    });
                }
            });
        });

        if (figureDataRef[0] == null || countVisualLoadedCategories(figureDataRef[0]) == 0) {
            itemsArea.removeAllViews();
            showVisualFigureLoader.run();
            uiHandler.postDelayed(visualInitialLoadWatchdog[0], 250L);
            loadVisualFigureData(data -> {
                figureDataRef[0] = data;
                if (!visualInitialContentRendered[0]) {
                    visualInitialContentRendered[0] = true;
                }
                hideVisualFigureLoader.run();
                refreshAll[0].run();
            }, () -> {
                finishVisualInitialLoadIfPossible[0].run();
                if (!visualInitialContentRendered[0]) {
                    hideVisualFigureLoader.run();
                    itemsArea.removeAllViews();
                    itemsArea.addView(centerNote(t(R.string.cannot_load_visuals)));
                }
            });
        } else {
            visualInitialContentRendered[0] = true;
            hideVisualFigureLoader.run();
            refreshAll[0].run();
        }

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            w.setWindowAnimations(0);
            w.setAttributes(params);
        }
    }


    private TextView visualCornerIconButton(Drawable drawable) {
        TextView btn = text("", 1, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, true);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(0, 0, 0, 0);
        btn.setIncludeFontPadding(false);
        btn.setBackground(drawable);
        btn.setClickable(true);
        btn.setFocusable(true);
        return btn;
    }

    private ArrayList<SavedVisualLook> loadSavedVisualLooks() {
        ArrayList<SavedVisualLook> out = new ArrayList<>();
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_SAVED_VISUALS, "[]");
        try {
            JSONArray arr = new JSONArray(raw == null || raw.trim().isEmpty() ? "[]" : raw);
            HashSet<String> seen = new HashSet<>();
            for (int i = 0; i < arr.length() && out.size() < MAX_SAVED_VISUALS; i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String figure = o.optString("figure", "").trim();
                if (figure.isEmpty() || seen.contains(figure)) continue;
                seen.add(figure);
                String gender = normalizeVisualGender(o.optString("gender", "M"), "M");
                out.add(new SavedVisualLook(figure, gender));
            }
        } catch(Exception ignored) {}
        return out;
    }

    private void saveSavedVisualLooks(ArrayList<SavedVisualLook> looks) {
        JSONArray arr = new JSONArray();
        try {
            if (looks != null) {
                HashSet<String> seen = new HashSet<>();
                for (SavedVisualLook look : looks) {
                    if (look == null || look.figure == null || look.figure.trim().isEmpty()) continue;
                    String figure = look.figure.trim();
                    if (seen.contains(figure)) continue;
                    seen.add(figure);
                    JSONObject o = new JSONObject();
                    o.put("figure", figure);
                    o.put("gender", normalizeVisualGender(look.gender, "M"));
                    arr.put(o);
                    if (arr.length() >= MAX_SAVED_VISUALS) break;
                }
            }
        } catch(Exception ignored) {}
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_SAVED_VISUALS, arr.toString()).apply();
    }

    private void saveVisualEditorLook(String figure, String gender) {
        String cleanFigure = figure == null ? "" : figure.trim();
        if (cleanFigure.isEmpty()) return;
        ArrayList<SavedVisualLook> looks = loadSavedVisualLooks();
        for (SavedVisualLook look : looks) {
            if (look != null && cleanFigure.equals(look.figure)) {
                toast(t(R.string.visual_already_saved));
                return;
            }
        }
        if (looks.size() >= MAX_SAVED_VISUALS) {
            toast(tr(R.string.visual_saved_limit, MAX_SAVED_VISUALS));
            return;
        }
        String detectedGender = detectVisualGenderFromFigure(cleanFigure, visualFigureDataCache, normalizeVisualGender(gender, "M"));
        looks.add(0, new SavedVisualLook(cleanFigure, detectedGender));
        saveSavedVisualLooks(looks);
        toast(t(R.string.visual_saved));
    }

    private void showSavedVisualsDialog(final String[] currentFigure, final String[] currentGender, final String[] currentType, final JSONObject[] figureDataRef, final Runnable[] refreshAll) {
        final Dialog savedDialog = new Dialog(this);
        LinearLayout rootDialog = new LinearLayout(this);
        rootDialog.setOrientation(LinearLayout.VERTICAL);
        rootDialog.setPadding(dp(18), dp(18), dp(18), dp(18));
        rootDialog.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        savedDialog.setContentView(rootDialog);
        applySafeAreaInsets(savedDialog.getWindow(), rootDialog);

        TextView title = habboText(t(R.string.saved_visuals), 22, true);
        title.setGravity(Gravity.CENTER);
        rootDialog.addView(title, lp(-1, -2, 0, 0, 0, 14));

        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(false);
        sv.setVerticalScrollBarEnabled(true);
        tintScrollBar(sv);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list, new ScrollView.LayoutParams(-1, -2));
        rootDialog.addView(sv, lp(-1, Math.min(dp(430), getResources().getDisplayMetrics().heightPixels - dp(220)), 0, 0, 0, 14));

        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            list.removeAllViews();
            ArrayList<SavedVisualLook> looks = loadSavedVisualLooks();
            if (looks.isEmpty()) {
                list.addView(centerNote(t(R.string.no_saved_visuals)));
                return;
            }
            for (int i = 0; i < looks.size(); i++) {
                final int index = i;
                final SavedVisualLook look = looks.get(i);
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(10), dp(8), dp(10), dp(8));
                row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(20,255,255,255), dp(16), lightTheme ? Color.rgb(218,218,218) : Color.argb(30,255,255,255), 1));
                list.addView(row, lp(-1, dp(86), 0, 0, 0, 8));

                ImageView avatar = new ImageView(this);
                avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
                loadAvatarImageKeepingCurrent(avatar, avatarMedium(look.figure, 2));
                row.addView(avatar, new LinearLayout.LayoutParams(dp(66), dp(76)));

                LinearLayout mid = new LinearLayout(this);
                mid.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, -2, 1);
                mp.leftMargin = dp(10);
                row.addView(mid, mp);

                TextView label = habboText("#" + (index + 1), 16, true);
                label.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
                mid.addView(label, lp(-1, -2, 0, 0, 0, 2));

                TextView code = text(look.figure, 11, lightTheme ? Color.rgb(78,78,86) : Color.argb(185,255,255,255), false);
                code.setSingleLine(true);
                code.setEllipsize(TextUtils.TruncateAt.END);
                mid.addView(code, new LinearLayout.LayoutParams(-1, -2));

                TextView remove = text("", 18, Color.WHITE, true);
                remove.setGravity(Gravity.CENTER);
                remove.setBackground(new RemoveXDrawable());
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(dp(38), dp(38));
                rp.leftMargin = dp(8);
                row.addView(remove, rp);

                row.setOnClickListener(v -> {
                    Runnable applyLook = () -> {
                        if (currentFigure != null && currentFigure.length > 0) currentFigure[0] = look.figure;
                        if (currentGender != null && currentGender.length > 0) {
                            JSONObject data = figureDataRef == null ? null : figureDataRef[0];
                            String selectedGenderFallback = normalizeVisualGender(currentGender[0], "M");
                            currentGender[0] = detectVisualGenderFromFigure(look.figure, data, selectedGenderFallback);
                        }
                        if (currentType != null && currentType.length > 0) currentType[0] = "hd";
                        visualItemViewsSessionCache.clear();
        visualItemRenderLimits.clear();
                        try { savedDialog.dismiss(); } catch(Exception ignored) {}
                        if (refreshAll != null && refreshAll.length > 0 && refreshAll[0] != null) refreshAll[0].run();
                    };
                    if ((figureDataRef == null || figureDataRef[0] == null) && visualFigureDataCache == null) {
                        loadVisualFigureData(data -> {
                            if (figureDataRef != null) figureDataRef[0] = data;
                            applyLook.run();
                        }, applyLook);
                    } else {
                        if (figureDataRef != null && figureDataRef[0] == null) figureDataRef[0] = visualFigureDataCache;
                        applyLook.run();
                    }
                });

                remove.setOnClickListener(v -> {
                    ArrayList<SavedVisualLook> fresh = loadSavedVisualLooks();
                    if (index >= 0 && index < fresh.size()) fresh.remove(index);
                    saveSavedVisualLooks(fresh);
                    toast(t(R.string.visual_removed));
                    render[0].run();
                });
            }
        };
        render[0].run();

        TextView close = dialogButton(t(R.string.close));
        close.setOnClickListener(v -> savedDialog.dismiss());
        rootDialog.addView(close, lp(-1, dp(48), 0, 0, 0, 0));

        savedDialog.show();
        Window w = savedDialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(28), dp(430));
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(params);
        }
    }

    private static class SavedVisualLook {
        final String figure;
        final String gender;
        SavedVisualLook(String figure, String gender) {
            this.figure = figure == null ? "" : figure.trim();
            this.gender = gender == null ? "M" : gender.trim();
        }
    }

    private File visualFigureDataDiskCacheFile() {
        return new File(getFilesDir(), VISUAL_FIGUREDATA_DISK_CACHE_FILE);
    }

    private JSONObject readVisualFigureDataDiskCache(boolean requireFresh) {
        File file = visualFigureDataDiskCacheFile();
        try {
            if (file == null || !file.exists() || file.length() <= 0L) return null;
            long age = System.currentTimeMillis() - Math.max(0L, file.lastModified());
            if (requireFresh && age > VISUAL_FIGUREDATA_CACHE_TTL_MS) return null;
            StringBuilder sb = new StringBuilder((int)Math.min(file.length(), 1024L * 1024L));
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 32768);
            try {
                char[] buf = new char[32768];
                int n;
                while ((n = br.read(buf)) > 0) sb.append(buf, 0, n);
            } finally {
                try { br.close(); } catch(Exception ignored) {}
            }
            JSONObject data = new JSONObject(sb.toString());
            return hasVisualPreloadedCategories(data) ? data : null;
        } catch(Exception ignored) {
            return null;
        }
    }

    private void writeVisualFigureDataDiskCache(JSONObject data) {
        if (data == null || !hasVisualPreloadedCategories(data)) return;
        try {
            File file = visualFigureDataDiskCacheFile();
            File tmp = new File(file.getParentFile(), VISUAL_FIGUREDATA_DISK_CACHE_FILE + ".tmp");
            FileOutputStream fos = new FileOutputStream(tmp, false);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
            try {
                writer.write(data.toString());
                writer.flush();
                try { fos.getFD().sync(); } catch(Exception ignored) {}
            } finally {
                try { writer.close(); } catch(Exception ignored) {}
            }
            if (file.exists()) {
                try { file.delete(); } catch(Exception ignored) {}
            }
            if (!tmp.renameTo(file)) {
                FileInputStream in = new FileInputStream(tmp);
                FileOutputStream out = new FileOutputStream(file, false);
                try {
                    byte[] buf = new byte[32768];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                    out.flush();
                    try { out.getFD().sync(); } catch(Exception ignored) {}
                } finally {
                    try { in.close(); } catch(Exception ignored) {}
                    try { out.close(); } catch(Exception ignored) {}
                    try { tmp.delete(); } catch(Exception ignored) {}
                }
            }
            try { file.setLastModified(System.currentTimeMillis()); } catch(Exception ignored) {}
        } catch(Exception ignored) {}
    }

    private void loadVisualFigureData(final VisualDataCallback ok, final Runnable fail) {
        if (visualFigureDataCache != null && System.currentTimeMillis() - visualFigureDataLoadedAt < 86400000L && hasVisualPreloadedCategories(visualFigureDataCache)) {
            if (ok != null) ok.onLoaded(visualFigureDataCache);
            return;
        }

        final JSONObject data = visualFigureDataCache != null ? visualFigureDataCache : createVisualFigureDataShell();
        visualFigureDataCache = data;
        visualFigureDataLoadedAt = System.currentTimeMillis();

        // Mesmo usando a base pronta do site, não transforme o JSON inteiro em JSONObject no Android.
        // Isso é o que causava demora/travamento após limpar cache. Extraímos as categorias por streaming
        // diretamente de https://atoxic.com.br/cache/figuredata-ui.json, igual ao fluxo rápido da v14.
        loadAllVisualCategoriesInto(data, () -> {
            if (ok != null) ok.onLoaded(data);
        }, () -> {
            if (visualCategory(data, "hd") != null || countVisualLoadedCategories(data) > 0) {
                if (ok != null) ok.onLoaded(data);
            } else if (fail != null) {
                fail.run();
            }
        });
    }

    private boolean hasVisualPreloadedCategories(JSONObject data) {
        try {
            if (data == null) return false;
            for (String type : VISUAL_PRELOAD_TYPES) {
                if (visualCategory(data, type) == null) return false;
            }
            return true;
        } catch(Exception ignored) {
            return false;
        }
    }

    private int countVisualLoadedCategories(JSONObject data) {
        try {
            JSONObject cats = data == null ? null : data.optJSONObject("categories");
            return cats == null ? 0 : cats.length();
        } catch(Exception ignored) {
            return 0;
        }
    }

    private void loadAllVisualCategoriesInto(final JSONObject data, final Runnable ok, final Runnable fail) {
        executor.submit(() -> {
            int loaded = 0;
            int failed = 0;
            try {
                for (String type : VISUAL_PRELOAD_TYPES) {
                    if (type == null || type.trim().isEmpty()) continue;
                    if (visualCategory(data, type) != null) {
                        loaded++;
                        continue;
                    }
                    try {
                        JSONObject category = loadVisualCategoryFromServer(type);
                        if (category == null || category.optJSONArray("items") == null) throw new RuntimeException("invalid category");
                        synchronized (data) {
                            JSONObject cats = data.optJSONObject("categories");
                            if (cats == null) {
                                cats = new JSONObject();
                                data.put("categories", cats);
                            }
                            cats.put(type, category);
                        }
                        loaded++;
                    } catch(Exception e) {
                        failed++;
                    }
                }
                visualFigureDataCache = data;
                visualFigureDataLoadedAt = System.currentTimeMillis();
                final int finalLoaded = loaded;
                runOnUiThread(() -> {
                    if (finalLoaded > 0) {
                        if (ok != null) ok.run();
                    } else {
                        if (fail != null) fail.run();
                    }
                });
            } catch(Exception e) {
                runOnUiThread(() -> {
                    if (countVisualLoadedCategories(data) > 0) {
                        if (ok != null) ok.run();
                    } else {
                        if (fail != null) fail.run();
                    }
                });
            }
        });
    }

    private JSONObject createVisualFigureDataShell() {
        JSONObject data = new JSONObject();
        try {
            data.put("ok", true);
            data.put("categories", new JSONObject());
        } catch(Exception ignored) {}
        return data;
    }

    private void loadVisualCategoryDataInto(final JSONObject data, final String rawType, final Runnable ok, final Runnable fail) {
        final String type = rawType == null ? "" : rawType.trim();
        if (type.isEmpty()) {
            if (fail != null) runOnUiThread(fail);
            return;
        }

        try {
            if (visualCategory(data, type) != null) {
                if (ok != null) runOnUiThread(ok);
                return;
            }
        } catch(Exception ignored) {}

        final String loadingKey = currentHotelKey + ":" + type;
        if (!visualCategoryLoading.add(loadingKey)) {
            waitForVisualCategoryLoaded(data, type, loadingKey, ok, fail, System.currentTimeMillis());
            return;
        }

        executor.submit(() -> {
            try {
                JSONObject category = loadVisualCategoryFromServer(type);
                if (category == null || category.optJSONArray("items") == null) throw new RuntimeException("invalid category");
                synchronized (data) {
                    JSONObject cats = data.optJSONObject("categories");
                    if (cats == null) {
                        cats = new JSONObject();
                        data.put("categories", cats);
                    }
                    cats.put(type, category);
                }
                visualFigureDataCache = data;
                visualFigureDataLoadedAt = System.currentTimeMillis();
                runOnUiThread(() -> { if (ok != null) ok.run(); });
            } catch(Exception e) {
                runOnUiThread(() -> { if (fail != null) fail.run(); });
            } finally {
                visualCategoryLoading.remove(loadingKey);
            }
        });
    }

    private void waitForVisualCategoryLoaded(final JSONObject data, final String type, final String loadingKey, final Runnable ok, final Runnable fail, final long startedAt) {
        uiHandler.postDelayed(() -> {
            try {
                if (visualCategory(data, type) != null) {
                    if (ok != null) ok.run();
                    return;
                }
            } catch(Exception ignored) {}

            boolean stillLoading = visualCategoryLoading.contains(loadingKey);
            if (!stillLoading || System.currentTimeMillis() - startedAt > 18000L) {
                if (fail != null) fail.run();
                return;
            }
            waitForVisualCategoryLoaded(data, type, loadingKey, ok, fail, startedAt);
        }, 300L);
    }

    private JSONObject loadVisualCategoryFromServer(String type) throws Exception {
        String encType = URLEncoder.encode(type, "UTF-8");

        // Primeiro tenta endpoints pequenos/específicos. Se o servidor ignorar o parâmetro
        // e devolver o figuredata inteiro, o leitor limitado interrompe antes de pesar a memória.
        String[] directUrls = new String[] {
            VISUAL_FIGUREDATA_URL + "&category=" + encType,
            VISUAL_FIGUREDATA_URL + "&type=" + encType
        };

        for (String url : directUrls) {
            try {
                String body = getTextLimited(url, 10000, 16000, 4_000_000);
                JSONObject cat = parseVisualCategoryResponse(body, type);
                if (cat != null && cat.optJSONArray("items") != null) return cat;
            } catch(Exception ignored) {}
        }

        // Depois usa exatamente a base que o site /visuais usa, mas extraindo por streaming.
        // Isso evita transformar um JSON de dezenas de MB em JSONObject no Samsung A10s.
        String[] fullUrls = new String[] {
            VISUAL_FIGUREDATA_CACHE_URL,
            VISUAL_FIGUREDATA_URL
        };

        for (String url : fullUrls) {
            try {
                String categoryText = getVisualCategoryObjectTextFromUrl(url, type, 10000, isLowMemoryVisualDevice() ? 26000 : 42000);
                if (categoryText != null && !categoryText.trim().isEmpty()) {
                    JSONObject category = new JSONObject(categoryText);
                    if (category.optJSONArray("items") != null) return category;
                }
            } catch(Exception ignored) {}
        }

        throw new IOException("visual category not found: " + type);
    }

    private JSONObject parseVisualCategoryResponse(String body, String type) throws Exception {
        if (body == null || body.trim().isEmpty()) return null;
        String clean = body.trim();
        JSONObject parsed = new JSONObject(clean);
        JSONObject cat = visualCategory(parsed, type);
        if (cat != null && cat.optJSONArray("items") != null) return cat;
        JSONObject data = parsed.optJSONObject("data");
        if (data != null) {
            cat = visualCategory(data, type);
            if (cat != null && cat.optJSONArray("items") != null) return cat;
            if (data.optJSONArray("items") != null) return data;
        }
        if (parsed.optJSONArray("items") != null) return parsed;
        return null;
    }

    private String getTextLimited(String u, int connectTimeoutMs, int readTimeoutMs, int maxBytes) throws Exception {
        HttpURLConnection c = (HttpURLConnection)new URL(u).openConnection();
        c.setConnectTimeout(connectTimeoutMs);
        c.setReadTimeout(readTimeoutMs);
        c.setRequestProperty("Accept", "application/json, text/plain, */*");
        c.setRequestProperty("User-Agent", "ToxicSearchTool/1.0 Android");
        int code = c.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        if (code < 200 || code >= 300 || is == null) throw new IOException("HTTP " + code);
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(maxBytes, 262144));
        byte[] buf = new byte[8192];
        int n;
        int total = 0;
        while ((n = is.read(buf)) > 0) {
            total += n;
            if (total > maxBytes) throw new IOException("visual response too large");
            out.write(buf, 0, n);
        }
        String body = out.toString("UTF-8");
        if (body.trim().isEmpty()) throw new IOException("empty visual response");
        return body;
    }

    private String getVisualCategoryObjectTextFromUrl(String u, String categoryKey, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        HttpURLConnection c = (HttpURLConnection)new URL(u).openConnection();
        c.setConnectTimeout(connectTimeoutMs);
        c.setReadTimeout(readTimeoutMs);
        c.setRequestProperty("Accept", "application/json, text/plain, */*");
        c.setRequestProperty("User-Agent", "ToxicSearchTool/1.0 Android");
        int code = c.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        if (code < 200 || code >= 300 || is == null) throw new IOException("HTTP " + code);
        try {
            return extractVisualCategoryObjectTextStreaming(is, categoryKey);
        } finally {
            try { is.close(); } catch(Exception ignored) {}
        }
    }

    private String extractVisualCategoryObjectTextStreaming(InputStream is, String categoryKey) throws Exception {
        if (is == null || categoryKey == null || categoryKey.trim().isEmpty()) return null;
        final String categoriesPattern = "\"categories\"";
        final String categoryPattern = "\"" + categoryKey + "\"";
        Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 32768);
        char[] buf = new char[8192];
        int phase = 0; // 0=procura categories, 1=procura key, 2=apos key, 3=apos dois-pontos, 4=captura objeto
        int match = 0;
        int depth = 0;
        boolean inString = false;
        boolean esc = false;
        StringBuilder out = null;
        int n;
        while ((n = reader.read(buf)) > 0) {
            for (int i = 0; i < n; i++) {
                char ch = buf[i];
                if (phase == 0) {
                    if (ch == categoriesPattern.charAt(match)) {
                        match++;
                        if (match == categoriesPattern.length()) {
                            phase = 1;
                            match = 0;
                        }
                    } else {
                        match = (ch == categoriesPattern.charAt(0)) ? 1 : 0;
                    }
                    continue;
                }

                if (phase == 1) {
                    if (ch == categoryPattern.charAt(match)) {
                        match++;
                        if (match == categoryPattern.length()) {
                            phase = 2;
                            match = 0;
                        }
                    } else {
                        match = (ch == categoryPattern.charAt(0)) ? 1 : 0;
                    }
                    continue;
                }

                if (phase == 2) {
                    if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') continue;
                    if (ch == ':') {
                        phase = 3;
                    } else {
                        phase = 1;
                        match = 0;
                    }
                    continue;
                }

                if (phase == 3) {
                    if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') continue;
                    if (ch == '{') {
                        phase = 4;
                        out = new StringBuilder(262144);
                        out.append(ch);
                        depth = 1;
                        inString = false;
                        esc = false;
                    } else {
                        phase = 1;
                        match = 0;
                    }
                    continue;
                }

                if (phase == 4) {
                    out.append(ch);
                    if (inString) {
                        if (esc) { esc = false; continue; }
                        if (ch == '\\') { esc = true; continue; }
                        if (ch == '"') inString = false;
                        continue;
                    }
                    if (ch == '"') { inString = true; continue; }
                    if (ch == '{') depth++;
                    else if (ch == '}') {
                        depth--;
                        if (depth == 0) return out.toString();
                    }
                }
            }
        }
        return null;
    }

    private String getText(String u, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        HttpURLConnection c = (HttpURLConnection)new URL(u).openConnection();
        c.setConnectTimeout(connectTimeoutMs);
        c.setReadTimeout(readTimeoutMs);
        c.setRequestProperty("Accept", "application/json, text/plain, */*");
        c.setRequestProperty("User-Agent", "ToxicSearchTool/1.0 Android");
        int code = c.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        String body = readAll(is);
        if (code < 200 || code >= 300 || body == null || body.trim().isEmpty()) throw new IOException("HTTP " + code);
        return body;
    }

    private String extractVisualCategoryObjectText(String json, String categoryKey) {
        if (json == null || categoryKey == null || categoryKey.trim().isEmpty()) return null;
        int catKey = json.indexOf("\"categories\"");
        if (catKey < 0) return null;
        int catObjStart = json.indexOf('{', catKey);
        if (catObjStart < 0) return null;

        int i = catObjStart + 1;
        int len = json.length();
        while (i < len) {
            i = skipJsonWhitespaceAndCommas(json, i);
            if (i >= len || json.charAt(i) == '}') return null;
            if (json.charAt(i) != '"') { i++; continue; }
            int keyEnd = findJsonStringEnd(json, i);
            if (keyEnd < 0) return null;
            String key = json.substring(i + 1, keyEnd);
            i = skipJsonWhitespace(json, keyEnd + 1);
            if (i >= len || json.charAt(i) != ':') return null;
            i = skipJsonWhitespace(json, i + 1);
            if (i >= len) return null;

            if (categoryKey.equals(key) && json.charAt(i) == '{') {
                int end = findJsonBalancedEnd(json, i);
                if (end > i) return json.substring(i, end + 1);
                return null;
            }

            int valueEnd = skipJsonValue(json, i);
            if (valueEnd <= i) i++; else i = valueEnd;
        }
        return null;
    }

    private int skipJsonWhitespaceAndCommas(String s, int i) {
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == ',' || c == ' ' || c == '\n' || c == '\r' || c == '\t') i++;
            else break;
        }
        return i;
    }

    private int skipJsonWhitespace(String s, int i) {
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') i++;
            else break;
        }
        return i;
    }

    private int findJsonStringEnd(String s, int quoteStart) {
        boolean esc = false;
        for (int i = quoteStart + 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) { esc = false; continue; }
            if (c == '\\') { esc = true; continue; }
            if (c == '"') return i;
        }
        return -1;
    }

    private int findJsonBalancedEnd(String s, int start) {
        char open = s.charAt(start);
        char close = open == '{' ? '}' : ']';
        int depth = 0;
        boolean inString = false;
        boolean esc = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inString) {
                if (esc) { esc = false; continue; }
                if (c == '\\') { esc = true; continue; }
                if (c == '"') inString = false;
                continue;
            }
            if (c == '"') { inString = true; continue; }
            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private int skipJsonValue(String s, int start) {
        if (start >= s.length()) return start;
        char c = s.charAt(start);
        if (c == '{' || c == '[') {
            int end = findJsonBalancedEnd(s, start);
            return end < 0 ? s.length() : end + 1;
        }
        if (c == '"') {
            int end = findJsonStringEnd(s, start);
            return end < 0 ? s.length() : end + 1;
        }
        int i = start;
        while (i < s.length()) {
            c = s.charAt(i);
            if (c == ',' || c == '}') break;
            i++;
        }
        return i;
    }

    private interface VisualDataCallback {
        void onLoaded(JSONObject data);
    }

    private void renderVisualCategories(LinearLayout tabs, String[] currentType, String[] currentGender, String[] currentFigure, JSONObject data, Runnable refresh) {
        if (tabs == null) return;
        tabs.removeAllViews();

        HorizontalScrollView mainScroll = new HorizontalScrollView(this);
        mainScroll.setHorizontalScrollBarEnabled(false);
        mainScroll.setFillViewport(true);
        LinearLayout mainRow = new LinearLayout(this);
        mainRow.setOrientation(LinearLayout.HORIZONTAL);
        mainRow.setGravity(Gravity.CENTER);
        mainScroll.addView(mainRow, new HorizontalScrollView.LayoutParams(-1, dp(50)));
        tabs.addView(mainScroll, new LinearLayout.LayoutParams(-1, dp(52)));

        HorizontalScrollView subScroll = new HorizontalScrollView(this);
        subScroll.setHorizontalScrollBarEnabled(false);
        subScroll.setFillViewport(true);
        LinearLayout subRow = new LinearLayout(this);
        subRow.setOrientation(LinearLayout.HORIZONTAL);
        subRow.setGravity(Gravity.CENTER);
        subRow.setPadding(0, 0, 0, 0);
        subScroll.addView(subRow, new HorizontalScrollView.LayoutParams(-1, dp(50)));
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, dp(52));
        subLp.topMargin = dp(6);
        tabs.addView(subScroll, subLp);

        VisualGroup[] groups = visualCategoryGroups(data);
        String activeGroup = visualActiveGroup(currentType[0], groups);

        for (VisualGroup group : groups) {
            View item = visualIconTab(group.icon, group.id.equals(activeGroup), dp(48));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(55), dp(48));
            lp.rightMargin = dp(7);
            mainRow.addView(item, lp);
            item.setOnClickListener(v -> {
                String first = group.types.length > 0 ? group.types[0] : "hd";
                currentType[0] = first;
                if (refresh != null) refresh.run();
            });
        }

        VisualGroup active = visualFindGroup(activeGroup, groups);
        if (active == null) return;

        if (active.genderTabs) {
            View male = visualIconTab("https://lite.habbonews.net/ferramentas/visuais/male.png", "M".equals(currentGender[0]), dp(48));
            male.setAlpha("M".equals(currentGender[0]) ? 1f : 0.50f);
            LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(dp(55), dp(48));
            mlp.rightMargin = dp(7);
            subRow.addView(male, mlp);
            male.setOnClickListener(v -> {
                if (!"M".equals(currentGender[0])) {
                    currentGender[0] = "M";
                    currentType[0] = "hd";
                    currentFigure[0] = setFigurePart(currentFigure[0], "hd", figurePart(DEFAULT_VISUAL_FIGURE_MALE, "hd"));
                }
                if (refresh != null) refresh.run();
            });

            View female = visualIconTab("https://lite.habbonews.net/ferramentas/visuais/female.png", "F".equals(currentGender[0]), dp(48));
            female.setAlpha("F".equals(currentGender[0]) ? 1f : 0.50f);
            LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(dp(55), dp(48));
            flp.rightMargin = dp(7);
            subRow.addView(female, flp);
            female.setOnClickListener(v -> {
                if (!"F".equals(currentGender[0])) {
                    currentGender[0] = "F";
                    currentType[0] = "hd";
                    currentFigure[0] = setFigurePart(currentFigure[0], "hd", figurePart(DEFAULT_VISUAL_FIGURE_FEMALE, "hd"));
                }
                if (refresh != null) refresh.run();
            });
        } else {
            for (String type : active.types) {
                View item = visualIconTab(categoryIconUrl(type, true), type.equals(currentType[0]), dp(48));
                item.setAlpha(type.equals(currentType[0]) ? 1f : 0.50f);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(55), dp(48));
                lp.rightMargin = dp(7);
                subRow.addView(item, lp);
                item.setOnClickListener(v -> {
                    currentType[0] = type;
                    if (refresh != null) refresh.run();
                });
            }
        }
    }

    private void renderVisualItems(LinearLayout area, LinearLayout colors, String[] currentFigure, String[] gender, String[] currentType, JSONObject data, Runnable updatePreview) {
        if (area == null) return;
        if (colors != null) colors.removeAllViews();
        if (!visualItemTutorialRunning) visualItemTutorialTarget = null;

        String uiType = currentType[0];
        String itemType = getVisualItemTypeForUiCategory(uiType);
        JSONObject category = visualCategory(data, itemType);
        if (category == null) {
            if (colors != null) colors.setVisibility(View.GONE);
            area.removeAllViews();
            area.addView(visualFigureDataLoadingView(), new LinearLayout.LayoutParams(-1, dp(300)));
            loadVisualCategoryDataInto(data, itemType, () -> renderVisualItems(area, colors, currentFigure, gender, currentType, data, updatePreview), () -> {
                area.removeAllViews();
                area.addView(centerNote(t(R.string.cannot_load_visuals)));
            });
            return;
        }

        JSONArray items = category.optJSONArray("items");
        if (items == null || items.length() == 0) {
            if (colors != null) colors.setVisibility(View.GONE);
            area.removeAllViews();
            area.addView(centerNote(t(R.string.no_items_found)));
            return;
        }

        if (colors != null) colors.setVisibility(View.VISIBLE);
        area.removeAllViews();

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, 0, 0, 0);
        area.addView(container, new LinearLayout.LayoutParams(-1, -2));

        int perRow = 5;
        LinearLayout row = null;
        int shown = 0;
        int totalEligible = 0;
        String currentId = figurePartId(currentFigure[0], itemType);
        int cellSize = dp(54);
        int rowHeight = dp(62);
        int gap = dp(8);

        // Depois que o carregamento passou a usar o mesmo modelo do site, não limitamos mais
        // os itens por aparelho. Todas as peças elegíveis da categoria são exibidas de uma vez.
        int renderLimit = Integer.MAX_VALUE / 4;

        if (isVisualRemovableType(uiType)) {
            View remove = visualItemCell("", itemType, "0", currentFigure[0], true, currentId.isEmpty());
            remove.setOnClickListener(v -> {
                currentFigure[0] = removeFigurePart(currentFigure[0], itemType);
                if (updatePreview != null) updatePreview.run();
                renderVisualItems(area, colors, currentFigure, gender, currentType, data, updatePreview);
            });
            row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            container.addView(row, lp(-1, rowHeight, 0, dp(8), 0, 4));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(cellSize, cellSize);
            rp.rightMargin = gap;
            row.addView(remove, rp);
            shown = 1;
        }

        for (int i=0; i<items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null || !item.optBoolean("selectable", true)) continue;
            String g = firstText(item, "gender", "sex");
            if (!visualGenderMatches(g, gender[0])) continue;
            totalEligible++;
            if (totalEligible > renderLimit) continue;

            if (shown % perRow == 0 || row == null) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER);
                container.addView(row, lp(-1, rowHeight, 0, shown == 0 ? dp(8) : 0, 0, 4));
            }
            final JSONObject finalItem = item;
            final String itemId = firstText(item, "id");
            String previewFigure = applyFigureItem(visualPreviewBaseFigure(gender[0], itemType), itemType, item, null);
            View cell = visualItemCell("", itemType, itemId, previewFigure, false, itemId.equals(currentId));
            cell.setOnClickListener(v -> {
                currentFigure[0] = applyFigureItem(currentFigure[0], itemType, finalItem, null);
                if ("hd".equals(itemType)) {
                    gender[0] = normalizeVisualGender(firstText(finalItem, "gender", "sex"), gender[0]);
                }
                markVisualItemSelected(container, cell);
                if (updatePreview != null) updatePreview.run();
                renderVisualColors(colors, currentFigure, uiType, finalItem, updatePreview, null);
            });
            attachVisualItemLongPress(cell, itemType, itemId, previewFigure);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(cellSize, cellSize);
            cp.rightMargin = gap;
            row.addView(cell, cp);
            if (visualItemTutorialTarget == null) visualItemTutorialTarget = cell;
            shown++;
        }


        JSONObject selected = findVisualItemByFigure(category, currentFigure[0], itemType);
        if (selected != null) renderVisualColors(colors, currentFigure, uiType, selected, updatePreview, null);
    }

    private boolean isLowMemoryVisualDevice() {
        try {
            ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                // Limitações/paginação do Provador somente para celulares com 3 GB de RAM ou menos.
                // Uso uma margem pequena porque alguns aparelhos reportam um pouco abaixo/acima do valor nominal.
                if (Build.VERSION.SDK_INT >= 16 && mi.totalMem > 0) {
                    return mi.totalMem <= (long)(3.25d * 1024d * 1024d * 1024d);
                }
                if (Build.VERSION.SDK_INT >= 19 && am.isLowRamDevice()) return true;
                if (am.getMemoryClass() > 0 && am.getMemoryClass() <= 192) return true;
            }
        } catch(Exception ignored) {}
        return false;
    }

    private void syncVisualItemSelection(View root, String currentId, String currentFigure, String itemType) {
        if (root == null) return;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i=0; i<group.getChildCount(); i++) syncVisualItemSelection(group.getChildAt(i), currentId, currentFigure, itemType);
        }
        if (root instanceof FrameLayout && root.getTag() instanceof String) {
            String tag = (String)root.getTag();
            if (tag.startsWith("visual_item_cell:" + itemType + ":")) {
                String id = tag.substring(("visual_item_cell:" + itemType + ":").length());
                boolean selected = (currentId == null ? "" : currentId).equals(id);
                applyVisualItemCellStyle(root, selected);
            }
        }
    }

    private void markVisualItemSelected(View root, View selectedCell) {
        if (root == null) return;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i=0; i<group.getChildCount(); i++) markVisualItemSelected(group.getChildAt(i), selectedCell);
        }
        if (root instanceof FrameLayout && root.getTag() instanceof String && ((String)root.getTag()).startsWith("visual_item_cell:")) {
            applyVisualItemCellStyle(root, root == selectedCell);
        }
    }

    private void applyVisualItemCellStyle(View cell, boolean selected) {
        if (cell == null) return;
        int selectedStroke = Color.rgb(188, 74, 255);
        int normalStroke = Color.argb(lightTheme ? 24 : 20, 255, 255, 255);
        int fill = selected ? Color.argb(lightTheme ? 72 : 70, 168, 76, 255) : Color.argb(lightTheme ? 18 : 26, 255, 255, 255);
        cell.setBackground(round(fill, dp(12), selected ? selectedStroke : normalStroke, selected ? 2 : 1));
        if (Build.VERSION.SDK_INT >= 21) cell.setElevation(selected ? dp(4) : 0);
    }

    private void attachVisualItemLongPress(final View cell, final String type, final String itemId, final String previewFigure) {
        if (cell == null) return;
        final boolean[] triggered = {false};
        final float[] downX = {0f};
        final float[] downY = {0f};
        final Runnable[] pending = new Runnable[1];

        cell.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                triggered[0] = false;
                downX[0] = event.getX();
                downY[0] = event.getY();
                pending[0] = () -> {
                    triggered[0] = true;
                    try { v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS); } catch(Exception ignored) {}
                    showVisualItemInfoDialog(type, itemId, previewFigure);
                };
                uiHandler.postDelayed(pending[0], 500L);
                return false;
            }

            if (action == MotionEvent.ACTION_MOVE) {
                float dx = Math.abs(event.getX() - downX[0]);
                float dy = Math.abs(event.getY() - downY[0]);
                if (dx > dp(12) || dy > dp(12)) {
                    if (pending[0] != null) uiHandler.removeCallbacks(pending[0]);
                }
                return triggered[0];
            }

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (pending[0] != null) uiHandler.removeCallbacks(pending[0]);
                return triggered[0];
            }

            return false;
        });
    }

    private void showVisualItemInfoDialog(final String type, final String itemId, final String previewFigure) {
        final Dialog dialog = new Dialog(this);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        tintScrollBar(scroll);

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(18), dp(18), dp(18), dp(18));
        wrap.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        scroll.addView(wrap, new ScrollView.LayoutParams(-1, -2));
        dialog.setContentView(scroll);
        applySafeAreaInsets(dialog.getWindow(), scroll);

        FrameLayout previewStage = new FrameLayout(this);
        previewStage.setBackground(round(
                lightTheme ? Color.rgb(248,248,248) : Color.argb(22,255,255,255),
                dp(18),
                lightTheme ? Color.rgb(220,220,220) : Color.argb(35,255,255,255),
                1
        ));
        wrap.addView(previewStage, lp(-1, dp(164), 0, 0, 0, 12));

        LinearLayout previewLine = new LinearLayout(this);
        previewLine.setOrientation(LinearLayout.HORIZONTAL);
        previewLine.setGravity(Gravity.CENTER_VERTICAL);
        previewLine.setPadding(dp(14), dp(6), dp(18), dp(6));
        previewStage.addView(previewLine, new FrameLayout.LayoutParams(-1, -1));

        ImageView avatarImage = new ImageView(this);
        avatarImage.setAdjustViewBounds(true);
        avatarImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams avatarLp = new LinearLayout.LayoutParams(0, -1, 1);
        previewLine.addView(avatarImage, avatarLp);
        loadAvatarImageKeepingCurrent(avatarImage, avatarFull(previewFigure, 2));

        ImageView rarityThumbnail = new ImageView(this);
        rarityThumbnail.setAdjustViewBounds(true);
        rarityThumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
        rarityThumbnail.setVisibility(View.INVISIBLE);
        LinearLayout.LayoutParams rarityLp = new LinearLayout.LayoutParams(dp(50), dp(50));
        rarityLp.leftMargin = dp(8);
        rarityLp.rightMargin = dp(8);
        previewLine.addView(rarityThumbnail, rarityLp);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(info, lp(-1, -2, 0, 0, 0, 12));

        info.addView(visualPurpleLoader(""), lp(-1, dp(84), 0, 4, 0, 4));

        TextView close = dialogButton(t(R.string.close));
        wrap.addView(close, lp(-1, dp(46), 0, 4, 0, 0));
        close.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.90f);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(params);
        }

        executor.execute(() -> {
            JSONObject found = null;
            try {
                JSONObject payload = unwrap(getJson(habbodexFigureUrl(previewFigure)));
                ArrayList<JSONObject> clothes = normalizeClothingEntries(payload);
                enrichClothingEntriesWithHabbonews(clothes, previewFigure);
                for (JSONObject o : clothes) {
                    String slot = firstText(o, "_slot", "type", "partType", "category");
                    if (type.equals(slot)) { found = o; break; }
                }
                if (found == null && !clothes.isEmpty()) found = clothes.get(0);
            } catch(Exception ignored) {}

            final JSONObject itemInfo = found;
            runOnUiThread(() -> {
                info.removeAllViews();

                if (itemInfo == null) {
                    rarityThumbnail.setVisibility(View.INVISIBLE);
                    info.addView(visualItemInfoRow(t(R.string.item_name), ""));
                    return;
                }

                String code = firstText(itemInfo, "code", "classname", "className", "id");
                String name = clothingName(itemInfo, code);
                String collection = clothingLineName(itemInfo, "");

                info.addView(visualItemInfoRow(t(R.string.item_name), name));
                if (!collection.isEmpty()) {
                    info.addView(visualItemInfoRow(t(R.string.collection), collection));
                }

                rarityThumbnail.setImageDrawable(null);
                if (setClothingRarityIcon(rarityThumbnail, itemInfo)) {
                    rarityThumbnail.setContentDescription(t(R.string.rarity));
                } else {
                    rarityThumbnail.setVisibility(View.INVISIBLE);
                }
            });
        });
    }

    private LinearLayout visualItemInfoRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(9), dp(12), dp(9));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(22,255,255,255), dp(14), lightTheme ? Color.rgb(222,222,222) : Color.argb(30,255,255,255), 1));
        row.setLayoutParams(lp(-1, -2, 0, 0, 0, 8));

        TextView l = text(label, 12, themeMutedColor(), true);
        l.setGravity(Gravity.LEFT);
        row.addView(l, lp(-1, -2, 0, 0, 0, 2));

        TextView v = text(value == null ? "" : value, 14, lightTheme ? Color.rgb(35,35,35) : Color.WHITE, false);
        v.setGravity(Gravity.LEFT);
        v.setMaxLines(3);
        v.setEllipsize(TextUtils.TruncateAt.END);
        row.addView(v, lp(-1, -2, 0, 0, 0, 0));
        return row;
    }

    private View visualItemCell(String label, String type, String id, String figure, boolean remove, boolean selected) {
        FrameLayout outer = new FrameLayout(this);
        outer.setPadding(dp(4), dp(4), dp(4), dp(4));
        outer.setTag("visual_item_cell:" + type + ":" + id);
        applyVisualItemCellStyle(outer, selected);

        FrameLayout box = new FrameLayout(this);
        box.setClipChildren(true);
        box.setClipToPadding(true);
        box.setBackground(round(Color.TRANSPARENT, dp(10), Color.TRANSPARENT, 0));
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(dp(46), dp(46), Gravity.CENTER);
        outer.addView(box, bp);

        ImageView img = new ImageView(this);
        img.setAdjustViewBounds(false);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        img.setPadding(0, 0, 0, 0);
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(dp(46), dp(72), Gravity.CENTER);
        box.addView(img, ip);

        if (remove) {
            Glide.with(MainActivity.this).load("https://lite.habbonews.net/ferramentas/visuais/removable.png").into(img);
            img.setScaleX(0.60f);
            img.setScaleY(0.60f);
        } else if (figure != null && !figure.isEmpty()) {
            img.setScaleX(visualItemScale(type));
            img.setScaleY(visualItemScale(type));
            img.setTranslationY(dp(visualItemOffsetDp(type)));
            loadAvatarImageKeepingCurrent(img, avatarFull(figure, 2));
        }

        return outer;
    }

    private void renderVisualColors(LinearLayout colors, String[] currentFigure, String type, JSONObject item, Runnable updatePreview, Runnable refreshItems) {
        if (colors == null || item == null) return;
        colors.removeAllViews();

        JSONArray arr = item.optJSONArray("colors");
        final String itemType = getVisualItemTypeForUiCategory(type);
        if (!item.optBoolean("colorable", false) || arr == null || arr.length() == 0) {
            renderDisabledColorPlaceholder(colors);
            return;
        }

        colors.setAlpha(0.88f);
        colors.setEnabled(true);
        bindColorPanelTouchLock(colors);

        int count = Math.max(1, Math.min(2, item.optInt("colorCount", 1)));
        ArrayList<String> activeColors = figurePartColors(currentFigure[0], itemType);

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);
        horizontal.setFillViewport(true);
        final float[] colorAreaLastY = {0f};
        horizontal.setOnTouchListener((v, event) -> {
            ViewParent parent = v.getParent();
            while (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
                if (!(parent instanceof View)) break;
                parent = ((View) parent).getParent();
            }

            ScrollView target = findColorScrollTarget(horizontal, event.getX());
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                colorAreaLastY[0] = event.getY();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float y = event.getY();
                int dy = (int)(colorAreaLastY[0] - y);
                colorAreaLastY[0] = y;
                if (target != null) target.scrollBy(0, dy);
                return true;
            }
            return true;
        });
        LinearLayout columns = new LinearLayout(this);
        columns.setOrientation(LinearLayout.HORIZONTAL);
        columns.setGravity(Gravity.CENTER);
        horizontal.addView(columns, new HorizontalScrollView.LayoutParams(-1, -2));
        colors.addView(horizontal, lp(-1, dp(122), 0, 0, 0, 0));

        for (int slot=0; slot<count; slot++) {
            final int colorSlot = slot;
            ScrollView vertical = new ScrollView(this);
            vertical.setVerticalScrollBarEnabled(true);
            vertical.setScrollbarFadingEnabled(false);
            tintScrollBar(vertical);
            bindNestedScrollTouch(vertical);

            LinearLayout column = new LinearLayout(this);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.LEFT);
            column.setPadding(0, 0, 0, 0);
            vertical.addView(column, new ScrollView.LayoutParams(-1, -2));

            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(count == 1 ? dp(304) : dp(156), dp(116));
            if (slot > 0) colLp.leftMargin = dp(10);
            columns.addView(vertical, colLp);

            int perRow = count == 1 ? 13 : 6;
            LinearLayout row = null;
            int shown = 0;
            String activeColor = activeColors.size() > slot ? activeColors.get(slot) : "";

            for (int i=0; i<arr.length(); i++) {
                JSONObject c = arr.optJSONObject(i);
                if (c == null || !c.optBoolean("selectable", true)) continue;
                if (shown % perRow == 0 || row == null) {
                    row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.LEFT);
                    column.addView(row, lp(-2, dp(25), 0, 0, 0, 1));
                }
                String colorId = firstText(c, "id");
                String hex = firstText(c, "hex");
                boolean club = c.optBoolean("isClub", false) || c.optBoolean("club", false) || "1".equals(firstText(c, "club")) || "2".equals(firstText(c, "club"));
                View sw = visualColorCell(hex, club, colorId.equals(activeColor));
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(dp(20), dp(20));
                cp.rightMargin = dp(3);
                cp.topMargin = dp(2);
                row.addView(sw, cp);
                final float[] colorCellLastY = {0f};
                final boolean[] colorCellMoved = {false};
                sw.setOnTouchListener((view, event) -> {
                    ViewParent parent = view.getParent();
                    while (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                        if (!(parent instanceof View)) break;
                        parent = ((View) parent).getParent();
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        colorCellLastY[0] = event.getRawY();
                        colorCellMoved[0] = false;
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        float y = event.getRawY();
                        int dy = (int)(colorCellLastY[0] - y);
                        if (Math.abs(dy) > dp(1)) colorCellMoved[0] = true;
                        colorCellLastY[0] = y;
                        vertical.scrollBy(0, dy);
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (!colorCellMoved[0]) {
                            currentFigure[0] = applyFigureItemColorSlot(currentFigure[0], itemType, item, colorId, colorSlot);
                            markVisualColorSelected(vertical, view);
                            if (updatePreview != null) updatePreview.run();
                        }
                        return true;
                    }
                    return true;
                });
                shown++;
            }
        }
    }

    private String normalizeVisualHex(String hex) {
        String h = hex == null ? "ffffff" : hex.replace("#", "").trim();
        if (!h.matches("(?i)[0-9a-f]{6}")) return "ffffff";
        return h.toLowerCase(Locale.ROOT);
    }

    private void markVisualColorSelected(View root, View selectedCell) {
        if (root == null) return;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i=0; i<group.getChildCount(); i++) markVisualColorSelected(group.getChildAt(i), selectedCell);
        }
        if (root instanceof FrameLayout && root.getTag() instanceof String && ((String)root.getTag()).startsWith("visual_color:")) {
            applyVisualColorCellStyle((FrameLayout)root, root == selectedCell);
        }
    }

    private void applyVisualColorCellStyle(FrameLayout box, boolean active) {
        if (box == null) return;
        Object raw = box.getTag();
        String hex = "ffffff";
        if (raw instanceof String) {
            String s = (String)raw;
            int idx = s.indexOf(':');
            if (idx >= 0 && idx + 1 < s.length()) hex = s.substring(idx + 1);
        }
        int fill = colorFromHex(hex);
        int stroke = lightenColorForVisualBorder(fill, active ? 0.72f : 0.44f);
        box.setAlpha(1f);
        box.setBackground(round(fill, dp(5), stroke, 2));
        box.setTranslationY(active ? -dp(2) : 0);
        if (Build.VERSION.SDK_INT >= 21) box.setElevation(active ? dp(5) : 0);
    }

    private void restoreVisualColorScroll(LinearLayout colors, int slot, int scrollX, int scrollY) {
        if (colors == null) return;
        colors.postDelayed(() -> {
            try {
                if (colors.getChildCount() == 0) return;
                View h = colors.getChildAt(0);
                if (h instanceof HorizontalScrollView) {
                    ((HorizontalScrollView) h).setScrollX(scrollX);
                    if (((HorizontalScrollView) h).getChildCount() == 0) return;
                    View colsView = ((HorizontalScrollView) h).getChildAt(0);
                    if (colsView instanceof LinearLayout) {
                        LinearLayout cols = (LinearLayout) colsView;
                        if (slot >= 0 && slot < cols.getChildCount()) {
                            View v = cols.getChildAt(slot);
                            if (v instanceof ScrollView) ((ScrollView) v).setScrollY(scrollY);
                        }
                    }
                }
            } catch(Exception ignored) {}
        }, 60L);
    }

    private ScrollView findColorScrollTarget(HorizontalScrollView horizontal, float x) {
        try {
            if (horizontal == null || horizontal.getChildCount() == 0) return null;
            View child = horizontal.getChildAt(0);
            if (!(child instanceof LinearLayout)) return null;
            LinearLayout columns = (LinearLayout) child;
            if (columns.getChildCount() == 0) return null;

            float absoluteX = x + horizontal.getScrollX();
            for (int i = 0; i < columns.getChildCount(); i++) {
                View v = columns.getChildAt(i);
                if (v instanceof ScrollView && absoluteX >= v.getLeft() && absoluteX <= v.getRight()) {
                    return (ScrollView) v;
                }
            }
            for (int i = 0; i < columns.getChildCount(); i++) {
                View v = columns.getChildAt(i);
                if (v instanceof ScrollView) return (ScrollView) v;
            }
        } catch(Exception ignored) {}
        return null;
    }

    private void renderDisabledColorPlaceholder(LinearLayout colors) {
        if (colors == null) return;
        colors.setAlpha(0.24f);
        colors.setEnabled(false);

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        colors.addView(column, lp(-1, dp(92), 0, 0, 0, 0));

        for (int r=0; r<3; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            column.addView(row, lp(-1, dp(25), 0, 0, 0, 1));
            for (int c=0; c<13; c++) {
                View sw = visualColorCell("8b34d9", false, false);
                sw.setAlpha(0.55f);
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(dp(20), dp(20));
                cp.rightMargin = dp(3);
                cp.topMargin = dp(2);
                row.addView(sw, cp);
            }
        }
    }

    private View visualColorCell(String hex, boolean club, boolean active) {
        FrameLayout box = new FrameLayout(this);
        box.setTag("visual_color:" + normalizeVisualHex(hex));
        applyVisualColorCellStyle(box, active);
        if (club) {
            ImageView hc = new ImageView(this);
            hc.setImageResource(R.drawable.hcmini);
            hc.setAdjustViewBounds(true);
            hc.setScaleType(ImageView.ScaleType.FIT_CENTER);
            FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(dp(20), dp(10), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            hp.bottomMargin = 0;
            box.addView(hc, hp);
        }
        return box;
    }

    private JSONObject visualCategory(JSONObject data, String type) {
        if (data == null) return null;
        JSONObject cats = data.optJSONObject("categories");
        if (cats == null) return null;
        return cats.optJSONObject(type);
    }

    private JSONObject findVisualItemByFigure(JSONObject category, String figure, String type) {
        String id = figurePartId(figure, type);
        if (id.isEmpty()) return null;
        JSONArray items = category == null ? null : category.optJSONArray("items");
        if (items == null) return null;
        for (int i=0; i<items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item != null && id.equals(firstText(item, "id"))) return item;
        }
        return null;
    }

    private String applyFigureItem(String figure, String type, JSONObject item, String forcedColor) {
        if (item == null) return figure;
        String id = firstText(item, "id");
        if (id.isEmpty()) return figure;
        String old = figurePart(figure, type);
        ArrayList<String> colors = new ArrayList<>();
        if (old != null && !old.isEmpty()) {
            String[] bits = old.split("-");
            for (int i=2; i<bits.length; i++) if (!bits[i].trim().isEmpty()) colors.add(bits[i].trim());
        }
        int colorCount = Math.max(0, item.optInt("colorCount", item.optBoolean("colorable", false) ? 1 : 0));
        if (forcedColor != null && !forcedColor.trim().isEmpty()) {
            if (colors.isEmpty()) colors.add(forcedColor.trim());
            else colors.set(0, forcedColor.trim());
        }
        JSONArray itemColors = item.optJSONArray("colors");
        while (colors.size() < colorCount) {
            String first = firstSelectableColorId(itemColors);
            colors.add(first.isEmpty() ? "1" : first);
        }
        StringBuilder part = new StringBuilder(type + "-" + id);
        for (int i=0; i<Math.min(colorCount, colors.size()); i++) part.append("-").append(colors.get(i));
        return setFigurePart(figure, type, part.toString());
    }

    private String firstSelectableColorId(JSONArray colors) {
        if (colors == null) return "";
        for (int i=0; i<colors.length(); i++) {
            JSONObject c = colors.optJSONObject(i);
            if (c != null && c.optBoolean("selectable", true)) return firstText(c, "id");
        }
        return "";
    }

    private String figurePart(String figure, String type) {
        if (figure == null || type == null) return "";
        String[] parts = figure.split("\\.");
        for (String p : parts) if (p.startsWith(type + "-")) return p;
        return "";
    }

    private String figurePartId(String figure, String type) {
        String p = figurePart(figure, type);
        if (p.isEmpty()) return "";
        String[] bits = p.split("-");
        return bits.length > 1 ? bits[1] : "";
    }

    private String setFigurePart(String figure, String type, String part) {
        ArrayList<String> parts = new ArrayList<>();
        boolean replaced = false;
        if (figure != null && !figure.trim().isEmpty()) {
            for (String p : figure.split("\\.")) {
                if (p.trim().isEmpty()) continue;
                if (p.startsWith(type + "-")) {
                    if (part != null && !part.isEmpty()) parts.add(part);
                    replaced = true;
                } else {
                    parts.add(p);
                }
            }
        }
        if (!replaced && part != null && !part.isEmpty()) parts.add(part);
        return TextUtils.join(".", parts);
    }

    private String removeFigurePart(String figure, String type) {
        return setFigurePart(figure, type, "");
    }

    private boolean visualGenderMatches(String itemGender, String currentGender) {
        if (itemGender == null || itemGender.trim().isEmpty()) return true;
        String g = itemGender.trim().toUpperCase(Locale.ROOT);
        return "U".equals(g) || g.equals(normalizeVisualGender(currentGender, "M"));
    }

    private String normalizeVisualGender(String gender, String fallback) {
        if (gender == null) return fallback == null ? "M" : fallback;
        String g = gender.trim().toUpperCase(Locale.ROOT);
        if (g.startsWith("F")) return "F";
        if (g.startsWith("M")) return "M";
        return fallback == null ? "M" : fallback;
    }

    private String detectVisualGenderFromFigure(String figure, JSONObject figureData, String fallback) {
        String safeFallback = normalizeVisualGender(fallback, "M");
        String hdId = figurePartId(figure, "hd");
        if (hdId.isEmpty()) return safeFallback;
        try {
            JSONObject body = visualCategory(figureData, "hd");
            JSONArray items = body == null ? null : body.optJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.optJSONObject(i);
                    if (item == null) continue;
                    String itemId = firstText(item, "id", "setid", "figureId");
                    if (hdId.equals(itemId)) {
                        String rawGender = firstText(item, "gender", "sex");
                        String gender = normalizeVisualGender(rawGender, safeFallback);
                        if ("M".equals(gender) || "F".equals(gender)) return gender;
                    }
                }
            }
        } catch(Exception ignored) {}
        if (hdId.equals(figurePartId(DEFAULT_VISUAL_FIGURE_FEMALE, "hd"))) return "F";
        if (hdId.equals(figurePartId(DEFAULT_VISUAL_FIGURE_MALE, "hd"))) return "M";
        return safeFallback;
    }

    private String visualPreviewBaseFigure(String gender, String itemType) {
        String base = "F".equalsIgnoreCase(gender) ? DEFAULT_VISUAL_FIGURE_FEMALE : DEFAULT_VISUAL_FIGURE_MALE;
        // A página usa a figure padrão para a miniatura dos itens, não o visual carregado por nick.
        return base;
    }

    private ArrayList<String> figurePartColors(String figure, String type) {
        ArrayList<String> out = new ArrayList<>();
        String old = figurePart(figure, type);
        if (old != null && !old.isEmpty()) {
            String[] bits = old.split("-");
            for (int i=2; i<bits.length; i++) if (!bits[i].trim().isEmpty()) out.add(bits[i].trim());
        }
        return out;
    }

    private String getVisualItemTypeForUiCategory(String type) {
        if ("ca".equals(type)) return "cp";
        if ("cp".equals(type)) return "ca";
        return type == null ? "" : type;
    }

    private boolean isVisualRemovableType(String type) {
        return "hr".equals(type) || "ha".equals(type) || "he".equals(type) || "ea".equals(type) || "fa".equals(type)
                || "ch".equals(type) || "ca".equals(type) || "cc".equals(type) || "cp".equals(type)
                || "sh".equals(type) || "wa".equals(type) || "pt".equals(type) || "mc".equals(type);
    }

    private static class VisualGroup {
        String id, icon;
        String[] types;
        boolean genderTabs;
        VisualGroup(String id, String icon, boolean genderTabs, String... types) {
            this.id = id;
            this.icon = icon;
            this.genderTabs = genderTabs;
            this.types = types == null ? new String[0] : types;
        }
    }

    private VisualGroup[] visualCategoryGroups(JSONObject data) {
        ArrayList<VisualGroup> out = new ArrayList<>();
        addVisualGroupIfAvailable(out, data, new VisualGroup("body", categoryIconUrl("hd", false), true, "hd"));
        addVisualGroupIfAvailable(out, data, new VisualGroup("hair", categoryIconUrl("hr", false), false, "hr","ha","he","ea","fa"));
        addVisualGroupIfAvailable(out, data, new VisualGroup("tops", categoryIconUrl("ch", false), false, "ch","ca","cc","cp"));
        addVisualGroupIfAvailable(out, data, new VisualGroup("bottoms", categoryIconUrl("lg", false), false, "lg","sh","wa"));
        addVisualGroupIfAvailable(out, data, new VisualGroup("extras", categoryIconUrl("mc", false), false, "pt","mc"));
        return out.toArray(new VisualGroup[0]);
    }

    private void addVisualGroupIfAvailable(ArrayList<VisualGroup> out, JSONObject data, VisualGroup group) {
        // Mantém todas as categorias visíveis; os dados de cada subcategoria são carregados sob demanda.
        out.add(group);
    }

    private String visualActiveGroup(String type, VisualGroup[] groups) {
        for (VisualGroup g : groups) for (String t : g.types) if (t.equals(type)) return g.id;
        return groups.length > 0 ? groups[0].id : "body";
    }

    private VisualGroup visualFindGroup(String id, VisualGroup[] groups) {
        for (VisualGroup g : groups) if (g.id.equals(id)) return g;
        return groups.length > 0 ? groups[0] : null;
    }

    private String visualIconResource(String name) {
        return "android.resource://" + getPackageName() + "/drawable/" + name;
    }

    private String categoryIconUrl(String type) {
        return categoryIconUrl(type, false);
    }

    private String categoryIconUrl(String type, boolean sub) {
        if ("hd".equals(type)) return visualIconResource("body");
        if ("hr".equals(type)) return sub ? visualIconResource("hair_sn") : visualIconResource("hair");
        if ("ha".equals(type)) return visualIconResource("hats");
        if ("he".equals(type)) return visualIconResource("hair_accessories");
        if ("ea".equals(type)) return visualIconResource("glasses");
        if ("fa".equals(type)) return visualIconResource("moustaches");
        if ("ch".equals(type)) return sub ? visualIconResource("top") : visualIconResource("tops");
        if ("ca".equals(type)) return visualIconResource("chest");
        if ("cc".equals(type)) return visualIconResource("jackets");
        if ("cp".equals(type)) return visualIconResource("accessories");
        if ("pt".equals(type)) return visualIconResource("pets");
        if ("mc".equals(type)) return visualIconResource("misc");
        if ("lg".equals(type)) return sub ? visualIconResource("bottoms_sn") : visualIconResource("bottoms");
        if ("sh".equals(type)) return visualIconResource("shoes");
        if ("wa".equals(type)) return visualIconResource("belts");
        return visualIconResource("misc");
    }

    private View visualIconTab(String url, boolean active, int size) {
        FrameLayout box = new FrameLayout(this);
        box.setPadding(dp(4), dp(4), dp(4), dp(4));
        int fill = active ? Color.argb(lightTheme ? 96 : 92, 171, 77, 255) : Color.argb(lightTheme ? 24 : 34, 255, 255, 255);
        int stroke = active ? Color.rgb(190, 76, 255) : Color.argb(lightTheme ? 28 : 26, 255,255,255);
        box.setBackground(round(fill, dp(14), stroke, active ? 2 : 1));
        if (Build.VERSION.SDK_INT >= 21 && active) box.setElevation(dp(4));
        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int innerSize = visualTabIconInnerSize(url, size);
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(innerSize, innerSize, Gravity.CENTER);
        box.addView(img, ip);
        Glide.with(MainActivity.this).load(url).into(img);
        return box;
    }

    private int visualTabIconInnerSize(String url, int size) {
        int normal = size - dp(12);
        String u = url == null ? "" : url.toLowerCase(Locale.ROOT);
        if (u.contains("/misc") || u.endsWith("misc")
                || u.contains("/pets") || u.endsWith("pets")
                || u.contains("/jackets") || u.endsWith("jackets")
                || u.contains("/accessories") || u.endsWith("accessories")) {
            return Math.max(dp(26), size - dp(18));
        }
        return Math.max(dp(28), normal);
    }

    private float visualItemScale(String type) {
        if ("hd".equals(type)) return 1.02f;
        if ("hr".equals(type) || "ha".equals(type) || "he".equals(type) || "ea".equals(type)) return 1.58f;
        if ("fa".equals(type)) return 1.30f;
        if ("lg".equals(type)) return 1.48f;
        if ("sh".equals(type)) return 1.82f;
        if ("ch".equals(type) || "ca".equals(type) || "cc".equals(type) || "cp".equals(type)) return 1.34f;
        if ("wa".equals(type) || "pt".equals(type) || "mc".equals(type)) return 1.34f;
        return 1.15f;
    }

    private int visualItemOffsetDp(String type) {
        if ("hr".equals(type) || "ha".equals(type) || "he".equals(type) || "ea".equals(type)) return 18;
        if ("fa".equals(type)) return 4;
        if ("ch".equals(type) || "cp".equals(type) || "ca".equals(type)) return -10;
        if ("cc".equals(type)) return -14;
        if ("lg".equals(type)) return -22;
        if ("sh".equals(type)) return -38;
        if ("wa".equals(type)) return -16;
        if ("pt".equals(type)) return -20;
        if ("mc".equals(type)) return -14;
        return 0;
    }

    private String applyFigureItemColorSlot(String figure, String type, JSONObject item, String colorId, int slot) {
        if (item == null || colorId == null || colorId.trim().isEmpty()) return figure;
        String id = firstText(item, "id");
        if (id.isEmpty()) return figure;
        String old = figurePart(figure, type);
        ArrayList<String> colors = new ArrayList<>();
        if (old != null && !old.isEmpty()) {
            String[] bits = old.split("-");
            for (int i=2; i<bits.length; i++) if (!bits[i].trim().isEmpty()) colors.add(bits[i].trim());
        }
        int colorCount = Math.max(1, Math.min(2, item.optInt("colorCount", item.optBoolean("colorable", false) ? 1 : 0)));
        JSONArray itemColors = item.optJSONArray("colors");
        while (colors.size() < colorCount) {
            String first = firstSelectableColorId(itemColors);
            colors.add(first.isEmpty() ? "1" : first);
        }
        int s = Math.max(0, Math.min(colorCount - 1, slot));
        colors.set(s, colorId.trim());
        StringBuilder part = new StringBuilder(type + "-" + id);
        for (int i=0; i<colorCount; i++) part.append("-").append(colors.get(i));
        return setFigurePart(figure, type, part.toString());
    }

    private String visualCategoryName(String type) {
        if ("hr".equals(type)) return t(R.string.cat_hair);
        if ("hd".equals(type)) return t(R.string.cat_head);
        if ("ch".equals(type)) return t(R.string.cat_shirts);
        if ("lg".equals(type)) return t(R.string.cat_pants);
        if ("sh".equals(type)) return t(R.string.cat_shoes);
        if ("ha".equals(type)) return t(R.string.cat_hats);
        if ("he".equals(type)) return t(R.string.cat_accessories);
        if ("ea".equals(type)) return t(R.string.cat_face);
        if ("fa".equals(type)) return t(R.string.cat_face);
        if ("ca".equals(type)) return t(R.string.cat_coats);
        if ("cc".equals(type)) return t(R.string.cat_coats);
        if ("cp".equals(type)) return t(R.string.cat_prints);
        if ("wa".equals(type)) return t(R.string.cat_accessories);
        return type.toUpperCase(Locale.ROOT);
    }

    private int colorFromHex(String hex) {
        try {
            String h = hex == null ? "" : hex.trim();
            if (!h.startsWith("#")) h = "#" + h;
            return Color.parseColor(h);
        } catch(Exception e) {
            return Color.WHITE;
        }
    }

    private int lightenColorForVisualBorder(int color, float amount) {
        amount = Math.max(0f, Math.min(1f, amount));
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        r = Math.min(255, Math.round(r + (255 - r) * amount));
        g = Math.min(255, Math.round(g + (255 - g) * amount));
        b = Math.min(255, Math.round(b + (255 - b) * amount));
        return Color.argb(Math.max(230, a), r, g, b);
    }

    private void showSettingsDialog() {
        final Dialog dialog = new Dialog(this);
        PullDispatchFrameLayout full = new PullDispatchFrameLayout(this);
        full.setBackground(makeBg());

        ScrollView dialogScroll = new ScrollView(this);
        dialogScroll.setFillViewport(true);
        dialogScroll.setVerticalScrollBarEnabled(false);

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(18), dp(34), dp(18), dp(82));
        wrap.setBackgroundColor(Color.TRANSPARENT);
        dialogScroll.addView(wrap, new ScrollView.LayoutParams(-1, -1));
        full.addView(dialogScroll, new FrameLayout.LayoutParams(-1, -1));

        bindBottomNavigationAutoHide(
                dialogScroll,
                addBottomNavigation(full, 3, dialog)
        );
        dialog.setContentView(full);
        applySafeAreaInsets(dialog.getWindow(), full);

        TextView title = habboText(t(R.string.settings), 24, true);
        title.setGravity(Gravity.CENTER);
        wrap.addView(title, lp(-1, -2, 0, 0, 0, 18));

        LinearLayout favNotifyRow = new LinearLayout(this);
        favNotifyRow.setOrientation(LinearLayout.HORIZONTAL);
        favNotifyRow.setGravity(Gravity.CENTER_VERTICAL);
        favNotifyRow.setPadding(dp(12), dp(10), dp(12), dp(10));
        favNotifyRow.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(14), lightTheme ? Color.rgb(218,218,218) : Color.argb(28,255,255,255), 1));
        TextView favNotifyText = text(t(R.string.notify_favorite_online), 14, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, true);
        favNotifyText.setGravity(Gravity.CENTER_VERTICAL);
        favNotifyRow.addView(favNotifyText, new LinearLayout.LayoutParams(0, -2, 1));
        TextView favNotifyToggle = text("", 1, Color.TRANSPARENT, false);
        favNotifyToggle.setBackground(new AchievementSwitchDrawable(notifyFavoriteOnline));
        favNotifyRow.addView(favNotifyToggle, new LinearLayout.LayoutParams(dp(58), dp(34)));
        wrap.addView(favNotifyRow, lp(-1, -2, 0, 0, 0, 10));
        favNotifyRow.setOnClickListener(v -> {
            notifyFavoriteOnline = !notifyFavoriteOnline;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(PREF_NOTIFY_FAVORITE_ONLINE, notifyFavoriteOnline).apply();
            favNotifyToggle.setBackground(new AchievementSwitchDrawable(notifyFavoriteOnline));
            startFavoriteOnlineWatcher();
            updateFavoriteOnlineAlarm();
        });


        TextView hotelTitle = text(t(R.string.search_hotel), 13, themeMutedColor(), true);
        hotelTitle.setGravity(Gravity.CENTER);
        wrap.addView(hotelTitle, lp(-1, -2, 0, 0, 0, 8));

        LinearLayout hotelGrid = new LinearLayout(this);
        hotelGrid.setOrientation(LinearLayout.VERTICAL);
        addHotelButtonRow(hotelGrid, dialog, "br", "com", "es");
        addHotelButtonRow(hotelGrid, dialog, "de", "fr", "fi");
        addHotelButtonRow(hotelGrid, dialog, "it", "nl", "tr");
        wrap.addView(hotelGrid, lp(-1, -2, 0, 0, 0, 14));

        LinearLayout themeRow = new LinearLayout(this);
        themeRow.setOrientation(LinearLayout.HORIZONTAL);
        themeRow.setGravity(Gravity.CENTER);
        TextView lightBtn = text("", 1, Color.TRANSPARENT, false);
        TextView darkBtn = text("", 1, Color.TRANSPARENT, false);
        lightBtn.setGravity(Gravity.CENTER);
        darkBtn.setGravity(Gravity.CENTER);
        lightBtn.setBackground(new ThemeIconButtonDrawable(true, lightTheme));
        darkBtn.setBackground(new ThemeIconButtonDrawable(false, !lightTheme));
        LinearLayout.LayoutParams th1 = new LinearLayout.LayoutParams(dp(46), dp(46)); th1.rightMargin = dp(7);
        LinearLayout.LayoutParams th2 = new LinearLayout.LayoutParams(dp(46), dp(46)); th2.leftMargin = dp(7);
        themeRow.addView(lightBtn, th1);
        themeRow.addView(darkBtn, th2);
        wrap.addView(themeRow, lp(-1, dp(50), 0, 0, 0, 10));
        lightBtn.setOnClickListener(v -> {
            if (lightTheme) return;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("theme", "light").apply();
            lightTheme = true;
            openingSplashShownThisSession = true;
            applySystemBarsForTheme();
            rebuildUiPreservingProfile();
            showSettingsDialog();
            uiHandler.postDelayed(() -> {
                try { dialog.dismiss(); } catch (Exception ignored) {}
            }, 120L);
        });
        darkBtn.setOnClickListener(v -> {
            if (!lightTheme) return;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("theme", "dark").apply();
            lightTheme = false;
            openingSplashShownThisSession = true;
            applySystemBarsForTheme();
            rebuildUiPreservingProfile();
            showSettingsDialog();
            uiHandler.postDelayed(() -> {
                try { dialog.dismiss(); } catch (Exception ignored) {}
            }, 120L);
        });


        Space cacheBottomSpacer = new Space(this);
        wrap.addView(cacheBottomSpacer, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView info = text(t(R.string.app_cache) + ": ...", 13, muted, false);
        info.setGravity(Gravity.CENTER);
        info.setPadding(dp(10), dp(10), dp(10), dp(10));
        info.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(14), lightTheme ? Color.rgb(218,218,218) : Color.argb(28,255,255,255), 1));
        wrap.addView(info, lp(-1, -2, 0, 0, 0, 14));
        updateCacheStatsLabelAsync(info);

        TextView clear = dialogButton(t(R.string.clear_app_cache));
        clear.setBackground(grad(dp(14), Color.rgb(120, 36, 46), Color.rgb(210, 54, 77)));
        wrap.addView(clear, lp(-1, dp(48), 0, 0, 0, 10));
        clear.setOnClickListener(v -> {
            clear.setEnabled(false);
            info.setText(t(R.string.app_cache) + ": ...");
            clearProfileCache(() -> {
                updateCacheStatsLabelAsync(info);
                clear.setEnabled(true);
                toast(t(R.string.app_cache_cleared));
            });
        });

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            w.setWindowAnimations(0);
            w.setAttributes(params);
        }
    }




    private void handleMainPullToRefreshDispatch(MotionEvent event) {
        if (mainScroll == null || activeRenderedProfile == null || searchInProgress) {
            if (pullDragging) resetMainPullIndicator();
            return;
        }

        int action = event.getActionMasked();
        int trigger = dp(220);
        int maxPull = dp(190);

        if (action == MotionEvent.ACTION_DOWN) {
            pullStartY = event.getRawY();
            pullStartedAtTop = mainScroll.getScrollY() <= 0;
            pullReadyToRefresh = false;
            pullDragging = false;
            return;
        }

        if (action == MotionEvent.ACTION_MOVE && pullStartedAtTop) {
            float dy = event.getRawY() - pullStartY;
            if (dy <= 0f) {
                pullReadyToRefresh = false;
                resetMainPullIndicator();
                return;
            }
            if (mainScroll.getScrollY() > 0 && !pullDragging) return;

            pullDragging = true;
            float progressValue = Math.max(0f, Math.min(1f, dy / Math.max(1, trigger)));
            float elastic = elasticPullDistance(dy, trigger, maxPull);
            updateMainPullIndicator(progressValue, elastic);
            pullReadyToRefresh = progressValue >= 1f;
            return;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            boolean shouldRefresh = pullStartedAtTop && pullDragging && pullReadyToRefresh;
            pullStartedAtTop = false;
            pullReadyToRefresh = false;
            pullDragging = false;
            resetMainPullIndicator();
            if (shouldRefresh) refreshCurrentProfileWithCooldown(true);
        }
    }

    private float elasticPullDistance(float dy, int trigger, int maxPull) {
        return Math.min(maxPull, (float)(maxPull * (1d - (1d / (1d + (dy / Math.max(1, trigger)))))));
    }

    private void updateMainPullIndicator(float progressValue, float elastic) {
        if (pullRefreshChip == null) return;
        pullRefreshChip.setVisibility(View.VISIBLE);
        pullRefreshChip.animate().cancel();
        pullRefreshChip.setAlpha(Math.max(0.15f, progressValue));
        pullRefreshChip.setTranslationY(-dp(22) + (elastic * 0.40f));
        if (pullRefreshSpinner != null) pullRefreshSpinner.setProgressPct(progressValue);
        if (pullRefreshText != null) pullRefreshText.setText(progressValue >= 1f ? t(R.string.updating_profile) : t(R.string.updating_profile));
        if (mainScroll != null) {
            mainScroll.animate().cancel();
            mainScroll.setTranslationY(elastic);
        }
    }

    private void resetMainPullIndicator() {
        if (pullRefreshSpinner != null) pullRefreshSpinner.setProgressPct(0);
        if (mainScroll != null) mainScroll.animate().translationY(0f).setDuration(190L).start();
        if (pullRefreshChip != null) {
            pullRefreshChip.animate().cancel();
            pullRefreshChip.animate().alpha(0f).translationY(-dp(44)).setDuration(180L).withEndAction(() -> pullRefreshChip.setVisibility(View.GONE)).start();
        }
    }

    private void refreshCurrentProfileWithCooldown(boolean fromPull) {
        if (activeRenderedProfile == null) return;
        String nick = activeRenderedProfile.name == null || activeRenderedProfile.name.trim().isEmpty() ? activeRenderedProfile.searchedNick : activeRenderedProfile.name;
        String uniqueId = activeRenderedProfile.uniqueId == null ? "" : activeRenderedProfile.uniqueId.trim();
        if ((nick == null || nick.trim().isEmpty()) && uniqueId.isEmpty()) return;

        String displayNick = nick == null || nick.trim().isEmpty() ? uniqueId : nick.trim();
        String refreshKey = normalizeNickKey(uniqueId.isEmpty() ? displayNick : uniqueId);
        if (!searchInProgress && activeRenderedProfile != null && refreshKey.equals(currentLoadedNick) && normalizeHotelKey(activeRenderedProfile.hotelKey).equals(currentHotelKey)) {
            long now = System.currentTimeMillis();
            long wait = PROFILE_REFRESH_COOLDOWN_MS - (now - lastSameNickRefreshAt);
            if (wait > 0) {
                hidePullRefreshIndicator();
                toast(tr(R.string.wait_refresh, Math.max(1, (int)Math.ceil(wait / 1000.0))));
                return;
            }
        }

        setSearchTextProgrammatically(displayNick);
        if (fromPull) showPullRefreshIndicator();
        if (!uniqueId.isEmpty()) searchByUniqueId(uniqueId, displayNick);
        else search();
    }

    private void showPullRefreshIndicator() {
        if (pullRefreshChip == null) return;
        if (pullRefreshText != null) pullRefreshText.setText(t(R.string.updating_profile));
        if (pullRefreshSpinner != null) pullRefreshSpinner.setProgressPct(1f);
        pullRefreshChip.setVisibility(View.VISIBLE);
        pullRefreshChip.animate().cancel();
        pullRefreshChip.setAlpha(0f);
        pullRefreshChip.setTranslationY(-dp(40));
        pullRefreshChip.animate().alpha(1f).translationY(0).setDuration(180).start();
        if (mainScroll != null) {
            mainScroll.animate().cancel();
            mainScroll.animate().translationY(dp(34)).setDuration(150).withEndAction(() -> mainScroll.animate().translationY(0).setDuration(220).start()).start();
        }
    }

    private void hidePullRefreshIndicator() {
        if (pullRefreshSpinner != null) pullRefreshSpinner.setProgressPct(0);
        if (pullRefreshChip == null) return;
        pullRefreshChip.animate().cancel();
        pullRefreshChip.animate().alpha(0f).translationY(-dp(40)).setDuration(180).withEndAction(() -> pullRefreshChip.setVisibility(View.GONE)).start();
    }

    private String normalizeHotelKey(String hotel) {
        String h = hotel == null ? "" : hotel.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        if ("us".equals(h)) h = "com";
        String[] allowed = {"br","com","es","de","fr","fi","it","nl","tr"};
        for (String a : allowed) if (a.equals(h)) return h;
        return "";
    }

    private String defaultHotelForDeviceLocale() {
        String lang = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        if ("pt".equals(lang) || "BR".equalsIgnoreCase(country)) return "br";
        if ("es".equals(lang)) return "es";
        if ("de".equals(lang)) return "de";
        if ("fr".equals(lang)) return "fr";
        if ("fi".equals(lang)) return "fi";
        if ("it".equals(lang)) return "it";
        if ("nl".equals(lang)) return "nl";
        if ("tr".equals(lang)) return "tr";
        return "com";
    }

    private String hotelDomain(String key) {
        String h = normalizeHotelKey(key);
        if ("com".equals(h)) return "www.habbo.com";
        if ("es".equals(h)) return "www.habbo.es";
        if ("de".equals(h)) return "www.habbo.de";
        if ("fr".equals(h)) return "www.habbo.fr";
        if ("fi".equals(h)) return "www.habbo.fi";
        if ("it".equals(h)) return "www.habbo.it";
        if ("nl".equals(h)) return "www.habbo.nl";
        if ("tr".equals(h)) return "www.habbo.com.tr";
        return "www.habbo.com.br";
    }

    private String habbodexHotelCode(String key) {
        String h = normalizeHotelKey(key);
        return "com".equals(h) ? "us" : (h.isEmpty() ? "br" : h);
    }

    private String hotelLabel(String key) {
        String h = normalizeHotelKey(key);
        if ("com".equals(h)) return ".COM";
        if ("tr".equals(h)) return ".COM.TR";
        if (h.isEmpty()) h = "br";
        return "." + h.toUpperCase(Locale.ROOT);
    }

    private String hotelName(String key) {
        return tr(R.string.hotel_name, hotelLabel(key).toLowerCase(Locale.ROOT).replace(".com.tr", ".com.tr"));
    }

    private String hotelFlag(String key) {
        return hotelLabel(key);
    }

    private String currentLang() {
        String h = normalizeHotelKey(currentHotelKey);
        if ("com".equals(h)) return "en";
        if ("es".equals(h)) return "es";
        if ("de".equals(h)) return "de";
        if ("fr".equals(h)) return "fr";
        if ("fi".equals(h)) return "fi";
        if ("it".equals(h)) return "it";
        if ("nl".equals(h)) return "nl";
        if ("tr".equals(h)) return "tr";
        return "pt";
    }

    private Context translationContext = null;
    private String translationContextHotel = "";

    private static Locale uiLocaleForHotel(String hotelKey) {
        String hotel = hotelKey == null ? "" : hotelKey.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        if ("us".equals(hotel)) hotel = "com";
        if ("com".equals(hotel)) return Locale.US;
        if ("es".equals(hotel)) return new Locale("es", "ES");
        if ("de".equals(hotel)) return Locale.GERMANY;
        if ("fr".equals(hotel)) return Locale.FRANCE;
        if ("fi".equals(hotel)) return new Locale("fi", "FI");
        if ("it".equals(hotel)) return Locale.ITALY;
        if ("nl".equals(hotel)) return new Locale("nl", "NL");
        if ("tr".equals(hotel)) return new Locale("tr", "TR");
        return new Locale("pt", "BR");
    }

    private String formatCount(long value) {
        NumberFormat formatter = NumberFormat.getIntegerInstance(
                uiLocaleForHotel(currentHotelKey)
        );
        formatter.setGroupingUsed(true);
        return formatter.format(value);
    }

    private String formatNumericText(String raw) {
        String clean = raw == null ? "" : raw.trim();
        if (clean.isEmpty() || "—".equals(clean)) return clean;
        if (!clean.matches("^-?\\d+$")) return clean;
        try {
            return formatCount(Long.parseLong(clean));
        } catch(Exception ignored) {
            return clean;
        }
    }

    private static Context localizedContextForHotel(Context baseContext, String hotelKey) {
        if (baseContext == null) return null;
        Configuration configuration = new Configuration(baseContext.getResources().getConfiguration());
        configuration.setLocale(uiLocaleForHotel(hotelKey));
        return baseContext.createConfigurationContext(configuration);
    }

    private synchronized Context currentTranslationContext() {
        String hotel = normalizeHotelKey(currentHotelKey);
        if (hotel.isEmpty()) hotel = "br";
        if (translationContext == null || !hotel.equals(translationContextHotel)) {
            translationContext = localizedContextForHotel(this, hotel);
            translationContextHotel = hotel;
        }
        return translationContext == null ? this : translationContext;
    }

    private static String localizedStringStatic(Context context, String hotelKey, int resourceId, Object... args) {
        if (context == null) return "";
        Context localized = localizedContextForHotel(context, hotelKey);
        Context source = localized == null ? context : localized;
        try {
            return args == null || args.length == 0
                    ? source.getString(resourceId)
                    : source.getString(resourceId, args);
        } catch (Exception ignored) {
            return context.getString(resourceId);
        }
    }

    private String tr(int resourceId, Object... args) {
        Context context = currentTranslationContext();
        try {
            return context.getString(resourceId, args);
        } catch (Exception ignored) {
            return t(resourceId);
        }
    }

    private String t(int resourceId) {
        try {
            return currentTranslationContext().getString(resourceId);
        } catch (Exception ignored) {
            return getString(resourceId);
        }
    }

    private String habboApiUrl(String path) {
        if (path == null) path = "";
        if (!path.startsWith("/")) path = "/" + path;
        return "https://" + hotelDomain(currentHotelKey) + path;
    }

    private String habboImagingUrl(String path) {
        if (path == null) path = "";
        if (!path.startsWith("/")) path = "/" + path;
        return "https://" + hotelDomain(currentHotelKey) + path;
    }

    private String badgeImageUrl(String code) {
        return "https://images.habbo.com/c_images/album1584/" + enc(code) + ".png";
    }

    private void addHotelButtonRow(LinearLayout grid, Dialog dialog, String a, String b, String c) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        grid.addView(row, lp(-1, dp(46), 0, 0, 0, 8));
        addHotelButton(row, dialog, a, 0);
        addHotelButton(row, dialog, b, 1);
        addHotelButton(row, dialog, c, 2);
    }

    private void addHotelButton(LinearLayout row, Dialog dialog, String hotelKey, int pos) {
        boolean active = hotelKey.equals(currentHotelKey);
        LinearLayout btn = new LinearLayout(this);
        btn.setOrientation(LinearLayout.HORIZONTAL);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(6), 0, dp(6), 0);
        btn.setBackground(active ? grad(dp(12), purple2, purple) : round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(12), lightTheme ? Color.rgb(218,218,218) : Color.argb(28,255,255,255), 1));

        ImageView flag = new ImageView(this);
        flag.setImageDrawable(new HotelFlagDrawable(hotelKey));
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(dp(30), dp(20));
        fp.rightMargin = 0;
        btn.addView(flag, fp);

        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, dp(42), 1);
        if (pos > 0) bp.leftMargin = dp(6);
        row.addView(btn, bp);
        btn.setOnClickListener(v -> {
            currentHotelKey = normalizeHotelKey(hotelKey);
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_HOTEL, currentHotelKey).apply();
            dialog.dismiss();
            activeSearchToken++;
            searchInProgress = false;
            profileSectionsInProgress = false;
            inlineProgressPct = 0;
            inlineProgressMessage = "";
            currentLoadedNick = "";
            activeRenderedProfile = null;
            resultWrap.removeAllViews();
            rebuildUiPreservingProfile();
            toast(t(R.string.hotel_changed));
        });
    }

    private void rememberOpenedProfile(ProfileResult r) {
        if (r == null || r.name == null || r.name.trim().isEmpty()) return;
        String hotel = normalizeHotelKey(r.hotelKey);
        if (hotel.isEmpty()) hotel = currentHotelKey;
        String key = profileIdentityKey(hotel, r.uniqueId, r.name);
        // Um perfil progressivo pode ser redesenhado várias vezes; não regrava
        // o mesmo histórico em SharedPreferences a cada pequena atualização.
        if (key.equals(lastRememberedOpenedProfileKey)) return;
        lastRememberedOpenedProfileKey = key;
        for (int i = openedProfilesHistory.size() - 1; i >= 0; i--) {
            ProfileHistoryItem item = openedProfilesHistory.get(i);
            if (profileIdentityKey(item.hotelKey, item.uniqueId, item.nick).equals(key)) openedProfilesHistory.remove(i);
        }
        openedProfilesHistory.add(0, new ProfileHistoryItem(r.name, r.figure, hotel, r.uniqueId));
        while (openedProfilesHistory.size() > 50) openedProfilesHistory.remove(openedProfilesHistory.size() - 1);
        saveOpenedProfilesHistory();
    }

    private void showOpenedProfilesHistoryDialog() {
        final Dialog dialog = new Dialog(this);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(18), dp(18), dp(18), dp(18));
        wrap.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        dialog.setContentView(wrap);
        applySafeAreaInsets(dialog.getWindow(), wrap);

        TextView title = habboText(t(R.string.profile_history), 22, true);
        title.setGravity(Gravity.CENTER);
        wrap.addView(title, lp(-1, -2, 0, 0, 0, 12));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(true);
        sv.setScrollbarFadingEnabled(false);
        tintScrollBar(sv);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list, new ScrollView.LayoutParams(-1, -2));
        wrap.addView(sv, lp(-1, dp(360), 0, 0, 0, 14));

        if (openedProfilesHistory.isEmpty()) {
            list.addView(centerNote(t(R.string.no_history)));
        } else {
            for (ProfileHistoryItem item : new ArrayList<>(openedProfilesHistory)) {
                list.addView(openedProfileHistoryRow(item, dialog));
            }
        }

        TextView clear = dialogButton(t(R.string.clear_history));
        clear.setBackground(grad(dp(14), Color.rgb(120, 36, 46), Color.rgb(210, 54, 77)));
        wrap.addView(clear, lp(-1, dp(48), 0, 0, 0, 0));
        clear.setOnClickListener(v -> {
            openedProfilesHistory.clear();
            saveOpenedProfilesHistory();
            dialog.dismiss();
            toast(t(R.string.history_cleared));
        });

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(28), dp(430));
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(params);
        }
    }

    private LinearLayout openedProfileHistoryRow(ProfileHistoryItem item, Dialog dialog) {
        LinearLayout row = profileListRowBase(item, false);

        TextView remove = text("", 18, Color.WHITE, true);
        remove.setGravity(Gravity.CENTER);
        remove.setBackground(new RemoveXDrawable());
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(dp(38), dp(38));
        rp.leftMargin = dp(6);
        row.addView(remove, rp);
        remove.setOnClickListener(v -> {
            for (int i = openedProfilesHistory.size() - 1; i >= 0; i--) {
                ProfileHistoryItem h = openedProfilesHistory.get(i);
                if (favoriteKey(h).equals(favoriteKey(item))) openedProfilesHistory.remove(i);
            }
            saveOpenedProfilesHistory();
            if (dialog != null) {
                dialog.dismiss();
                showOpenedProfilesHistoryDialog();
            }
        });

        bindProfileCardOpenAndHold(row, item.nick, item.hotelKey, item.figure, item.uniqueId, () -> openProfileListItem(item, dialog));
        return row;
    }

    private void showBadgeDialog(JSONObject badge) {
        if (badge == null) return;
        String code = firstText(badge, "code", "badgeCode");
        if (code.isEmpty()) return;
        String name = firstText(badge, "name", "title");
        if (name.isEmpty()) name = code;
        String desc = firstText(badge, "description", "desc");
        if (desc.isEmpty()) desc = t(R.string.no_description);
        String created = badgeObtainedDate(badge);
        String owners = firstText(badge, "totalOwners", "owners", "ownerCount", "count");

        final Dialog dialog = new Dialog(this);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(14), dp(14), dp(14), dp(14));
        wrap.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        dialog.setContentView(wrap);
        applySafeAreaInsets(dialog.getWindow(), wrap);

        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        img.setPadding(dp(30), dp(24), dp(30), dp(24));
        img.setBackground(round(lightTheme ? Color.rgb(245,245,245) : Color.argb(20,255,255,255), dp(16), lightTheme ? Color.rgb(224,224,224) : Color.argb(28,255,255,255), 1));
        wrap.addView(img, lp(-1, dp(170), 0,0,0,12));
        loadImage(img, badgeImageUrl(code));

        LinearLayout infoGrid = new LinearLayout(this);
        infoGrid.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(infoGrid, lp(-1, -2, 0, 0, 0, 0));
        infoGrid.addView(photoInfoCard(t(R.string.name), name, "", ""));
        infoGrid.addView(photoInfoCard(t(R.string.description), desc, "", ""));
        infoGrid.addView(photoInfoCard(t(R.string.obtained), created.isEmpty() ? "" : niceDateOnly(created), "", ""));
        if (!owners.isEmpty()) infoGrid.addView(photoInfoCard(t(R.string.total_owners), formatNumericText(owners), "", ""));
        infoGrid.addView(photoInfoCard(t(R.string.code), code, "", ""));

        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(shownWindow.getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            shownWindow.setAttributes(params);
        }
    }

    private boolean isSameProfileObject(JSONObject a, JSONObject b) {
        if (a == null || b == null) return false;
        if (a == b) return true;
        String aId = normalizeNickKey(firstText(a, "uniqueId", "habboUniqueId", "id", "habboId"));
        String bId = normalizeNickKey(firstText(b, "uniqueId", "habboUniqueId", "id", "habboId"));
        return !aId.isEmpty() && aId.equals(bId);
    }

    private boolean isSameProfileId(String expectedUniqueId, JSONObject obj) {
        String expected = normalizeNickKey(expectedUniqueId);
        if (expected.isEmpty() || obj == null) return false;
        String actual = normalizeNickKey(firstText(
                obj, "uniqueId", "habboUniqueId", "id", "habboId"
        ));
        return !actual.isEmpty() && expected.equals(actual);
    }

    private void pushCurrentProfileToHistory(String nextNickKey) {
        if (activeRenderedProfile == null || activeRenderedProfile.name == null || activeRenderedProfile.name.trim().isEmpty()) return;
        String currentId = normalizeNickKey(activeRenderedProfile.uniqueId);
        String currentName = normalizeNickKey(activeRenderedProfile.name);
        if (normalizeHotelKey(activeRenderedProfile.hotelKey).equals(currentHotelKey) && ((!currentId.isEmpty() && currentId.equals(nextNickKey)) || (!currentName.isEmpty() && currentName.equals(nextNickKey)))) return;
        if (!profileHistory.isEmpty()) {
            ProfileResult last = profileHistory.peekLast();
            if (sameProfile(last, activeRenderedProfile)) return;
        }
        profileHistory.addLast(copyProfileResult(activeRenderedProfile));
        while (profileHistory.size() > PROFILE_HISTORY_LIMIT) profileHistory.removeFirst();
    }

    private boolean sameProfile(ProfileResult a, ProfileResult b) {
        if (a == null || b == null) return false;
        String aId = normalizeNickKey(a.uniqueId);
        String bId = normalizeNickKey(b.uniqueId);
        if (!aId.isEmpty() && !bId.isEmpty()) return aId.equals(bId);
        return normalizeNickKey(a.name).equals(normalizeNickKey(b.name));
    }

    private ProfileResult copyProfileResult(ProfileResult src) {
        ProfileResult c = new ProfileResult();
        if (src == null) return c;
        c.searchedNick = src.searchedNick; c.uniqueId = src.uniqueId; c.name = src.name; c.motto = src.motto; c.figure = src.figure; c.memberSince = src.memberSince; c.lastAccess = src.lastAccess; c.level = src.level; c.starGems = src.starGems; c.hotelKey = src.hotelKey;
        c.online = src.online; c.privateProfile = src.privateProfile; c.banned = src.banned;
        c.habboPublic = src.habboPublic; c.dex = src.dex; c.suggest = src.suggest; c.dexProfile = src.dexProfile; c.officialProfile = src.officialProfile; c.officialBadgeLookup = src.officialBadgeLookup;
        c.previousNames = new ArrayList<>(src.previousNames); c.previousMottos = new ArrayList<>(src.previousMottos); c.previousStyles = new ArrayList<>(src.previousStyles); c.photos = new ArrayList<>(src.photos); c.friends = new ArrayList<>(src.friends); c.oldFriends = new ArrayList<>(src.oldFriends); c.rooms = new ArrayList<>(src.rooms); c.oldRooms = new ArrayList<>(src.oldRooms); c.groups = new ArrayList<>(src.groups); c.badges = new ArrayList<>(src.badges); c.badgesWithAchievements = new ArrayList<>(src.badgesWithAchievements); c.totalBadges = src.totalBadges; c.selectedBadges = new ArrayList<>(src.selectedBadges);
        c.allPhotosSource = new ArrayList<>(src.allPhotosSource); c.allStylesSource = new ArrayList<>(src.allStylesSource);
        c.photosNextPage = src.photosNextPage; c.stylesNextPage = src.stylesNextPage; c.photosTotal = src.photosTotal; c.stylesTotal = src.stylesTotal; c.stylesRemoteNextPage = src.stylesRemoteNextPage;
        c.removedFriendsNextPage = src.removedFriendsNextPage; c.removedFriendsTotal = src.removedFriendsTotal; c.friendsNextPage = src.friendsNextPage; c.friendsTotal = src.friendsTotal; c.friendsTabPage = src.friendsTabPage; c.previousMottosSlideIndex = src.previousMottosSlideIndex; c.badgesNextPage = src.badgesNextPage; c.badgesTotal = src.badgesTotal; c.badgesTabPage = src.badgesTabPage;
        c.photosHasMore = src.photosHasMore; c.stylesHasMore = src.stylesHasMore; c.photosLoading = false; c.stylesLoading = false;
        c.removedFriendsHasMore = src.removedFriendsHasMore; c.removedFriendsLoading = false; c.friendsHasMore = src.friendsHasMore; c.friendsLoading = false; c.friendsPagedMode = src.friendsPagedMode; c.friendsTabShowingRemoved = src.friendsTabShowingRemoved; c.friendsTabSelectionTouched = src.friendsTabSelectionTouched; c.badgesHasMore = src.badgesHasMore; c.badgesLoading = false; c.badgesPagedMode = src.badgesPagedMode; c.hideAchievementBadges = src.hideAchievementBadges;
        c.officialProfileAttempted = src.officialProfileAttempted; c.officialPhotosAttempted = src.officialPhotosAttempted; c.officialPhotosSucceeded = src.officialPhotosSucceeded; c.photosFromOfficial = src.photosFromOfficial; c.stylesFromComplement = src.stylesFromComplement; c.stylesRemotePaged = src.stylesRemotePaged; c.friendsDatesReady = src.friendsDatesReady;
        return c;
    }

    @Override public void onBackPressed() {
        if (accessGateReason != AccessGateReason.NONE) return;
        if (searchInput != null && searchInput.hasFocus()) {
            clearSearchFocus();
            return;
        }
        if (!profileHistory.isEmpty()) {
            activeSearchToken++;
            searchInProgress = false;
            profileSectionsInProgress = false;
            activeSearchNick = "";
            inlineProgressPct = 0;
            inlineProgressMessage = "";
            ProfileResult previous = profileHistory.removeLast();
            String previousHotel = normalizeHotelKey(previous.hotelKey);
            if (!previousHotel.isEmpty()) {
                currentHotelKey = previousHotel;
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_HOTEL, currentHotelKey).apply();
            }
            updateSelectedHotelHeaderFlag();
            activeRenderedProfile = previous;
            currentLoadedNick = normalizeNickKey(previous.name);
            setSearchTextProgrammatically(previous.name == null ? "" : previous.name);
            clearSearchFocus();
            setStatusMessage("");
            renderProfile(previous);
            return;
        }
        super.onBackPressed();
    }

    private TextView dialogButton(String label) {
        TextView v = habboText(label, 15, true);
        v.setGravity(Gravity.CENTER);
        v.setTextColor(Color.WHITE);
        v.setPadding(dp(12), 0, dp(12), 0);
        v.setBackground(grad(dp(14), purple2, purple));
        return v;
    }





    private void updateFavoriteOnlineAlarm() {
        try {
            if (notifyFavoriteOnline) scheduleFavoriteOnlineAlarm();
            else cancelFavoriteOnlineAlarm();
        } catch(Exception ignored) {}
    }

    private PendingIntent favoriteOnlineAlarmIntent(int flags) {
        Intent intent = new Intent(this, FavoriteOnlineReceiver.class);
        intent.setAction("com.toxic.search.FAVORITE_ONLINE_CHECK");
        return PendingIntent.getBroadcast(this, 2607, intent, flags | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
    }

    private void scheduleFavoriteOnlineAlarm() {
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = favoriteOnlineAlarmIntent(PendingIntent.FLAG_UPDATE_CURRENT);
        long first = System.currentTimeMillis() + 60_000L;
        am.cancel(pi);
        am.setRepeating(AlarmManager.RTC_WAKEUP, first, 60_000L, pi);
    }

    private void cancelFavoriteOnlineAlarm() {
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = favoriteOnlineAlarmIntent(PendingIntent.FLAG_NO_CREATE);
        if (pi != null) am.cancel(pi);
    }


    private void loadFavoriteOnlineStatesFromPrefs() {
        try {
            String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_FAVORITE_ONLINE_STATES, "{}");
            JSONObject obj = new JSONObject(raw == null || raw.trim().isEmpty() ? "{}" : raw);
            favoriteOnlineStates.clear();
            for (ProfileHistoryItem item : favoriteProfiles) {
                if (item == null) continue;
                String key = favoriteKey(item);
                if (obj.has(key)) favoriteOnlineStates.put(key, obj.optBoolean(key, false));
            }
        } catch(Exception ignored) {}
    }

    private int favoriteOnlineCount() {
        int count = 0;
        for (ProfileHistoryItem item : favoriteProfiles) {
            if (item == null) continue;
            if (Boolean.TRUE.equals(favoriteOnlineStates.get(favoriteKey(item)))) count++;
        }
        return Math.max(0, Math.min(MAX_FAVORITES, count));
    }

    private void updateFavoriteOnlineBadgeText() {
        int count = favoriteOnlineCount();
        for (int i = favoriteOnlineBadgeViews.size() - 1; i >= 0; i--) {
            TextView badge = favoriteOnlineBadgeViews.get(i);
            if (badge == null || badge.getParent() == null) {
                favoriteOnlineBadgeViews.remove(i);
                continue;
            }
            int bw = count >= 10 ? dp(24) : dp(18);
            ViewGroup.LayoutParams rawLp = badge.getLayoutParams();
            if (rawLp != null && rawLp.width != bw) {
                rawLp.width = bw;
                badge.setLayoutParams(rawLp);
            }
            if (count <= 0) {
                badge.setVisibility(View.GONE);
            } else {
                badge.setTextColor(Color.WHITE);
                badge.setText(String.valueOf(Math.min(MAX_FAVORITES, count)));
                badge.setVisibility(View.VISIBLE);
            }
        }
    }

    private long favoriteOnlineWatcherIntervalMs() {
        return appInForeground ? FAVORITE_ONLINE_FOREGROUND_INTERVAL_MS : FAVORITE_ONLINE_BACKGROUND_INTERVAL_MS;
    }

    private void startFavoriteOnlineWatcher() {
        if (favoriteOnlineWatcher != null) uiHandler.removeCallbacks(favoriteOnlineWatcher);
        favoriteOnlineWatcher = () -> {
            checkFavoriteOnlineNotifications();
            uiHandler.postDelayed(favoriteOnlineWatcher, favoriteOnlineWatcherIntervalMs());
        };
        uiHandler.postDelayed(favoriteOnlineWatcher, appInForeground ? 500L : favoriteOnlineWatcherIntervalMs());
    }

    private void checkFavoriteOnlineNotifications() {
        if (favoriteProfiles.isEmpty()) {
            runOnUiThread(() -> updateFavoriteOnlineBadgeText());
            return;
        }
        ArrayList<ProfileHistoryItem> snapshot = new ArrayList<>(favoriteProfiles);
        executor.execute(() -> {
            boolean changedAny = false;
            for (ProfileHistoryItem item : snapshot) {
                if (item == null) continue;
                String key = favoriteKey(item);
                FavoriteStatus st = fetchFavoriteStatus(item);
                if (st == null) continue;

                String newKey = profileIdentityKey(st.hotelKey, st.uniqueId, st.nick);
                Boolean oldStored = getStoredFavoriteOnlineState(key);
                Boolean oldMemory = favoriteOnlineStates.get(key);
                boolean hadPrevious = oldStored != null || oldMemory != null;
                boolean wasOnline = oldStored != null ? oldStored.booleanValue() : Boolean.TRUE.equals(oldMemory);

                Boolean old = favoriteOnlineStates.put(key, st.online);
                favoriteStatusCache.put(key, st);
                if (!newKey.isEmpty()) {
                    favoriteOnlineStates.put(newKey, st.online);
                    favoriteStatusCache.put(newKey, st);
                }
                setStoredFavoriteOnlineState(key, st.online);
                if (!newKey.isEmpty()) setStoredFavoriteOnlineState(newKey, st.online);
                cacheFavoriteHeadAsync(st);
                if (old == null || old.booleanValue() != st.online) changedAny = true;

                if (notifyFavoriteOnline && hadPrevious && !wasOnline && st.online && isFavoriteRecentlyOnline(st)) {
                    runOnUiThread(() -> showFavoriteOnlineSystemNotification(st));
                }
            }
            if (changedAny || !snapshot.isEmpty()) runOnUiThread(() -> updateFavoriteOnlineBadgeText());
        });
    }


    private boolean isFavoriteRecentlyOnline(FavoriteStatus st) {
        if (st == null || st.lastAccess == null || st.lastAccess.trim().isEmpty()) return false;
        long access = parseHabboTimestampMs(st.lastAccess);
        if (access <= 0L) return false;
        long age = System.currentTimeMillis() - access;
        return age >= -30_000L && age <= 3L * 60L * 1000L;
    }

    private long parseHabboTimestampMs(String raw) {
        return parseHabboTimestampMsStatic(raw);
    }

    private Boolean getStoredFavoriteOnlineState(String key) {
        if (key == null || key.isEmpty()) return null;
        try {
            String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_FAVORITE_ONLINE_STATES, "{}");
            JSONObject obj = new JSONObject(raw == null || raw.trim().isEmpty() ? "{}" : raw);
            if (!obj.has(key)) return null;
            return obj.optBoolean(key, false);
        } catch(Exception ignored) { return null; }
    }

    private void setStoredFavoriteOnlineState(String key, boolean online) {
        if (key == null || key.isEmpty()) return;
        try {
            SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
            String raw = sp.getString(PREF_FAVORITE_ONLINE_STATES, "{}");
            JSONObject obj = new JSONObject(raw == null || raw.trim().isEmpty() ? "{}" : raw);
            obj.put(key, online);
            sp.edit().putString(PREF_FAVORITE_ONLINE_STATES, obj.toString()).apply();
        } catch(Exception ignored) {}
    }

    private FavoriteStatus fetchFavoriteStatus(ProfileHistoryItem item) {
        if (item == null) return null;
        try {
            String hotel = normalizeHotelKey(item.hotelKey);
            if (hotel.isEmpty()) hotel = "br";
            JSONObject obj = null;
            String storedId = item.uniqueId == null ? "" : item.uniqueId.trim();
            if (!storedId.isEmpty()) {
                obj = validProfileObject(tryJson(
                        "https://" + hotelDomain(hotel) + "/api/public/users/" + enc(storedId)
                ));
            }
            if (obj == null && item.nick != null && !item.nick.trim().isEmpty()) {
                obj = validProfileObject(tryJson("https://" + hotelDomain(hotel) + "/api/public/users?name=" + enc(item.nick)));
            }
            if (obj == null) return null;
            FavoriteStatus st = new FavoriteStatus();
            st.nick = firstText(obj, "name", "username", "habboName");
            if (st.nick.isEmpty()) st.nick = item.nick;
            st.uniqueId = firstText(obj, "uniqueId", "id", "habboId");
            if (st.uniqueId.isEmpty()) st.uniqueId = storedId;
            st.figure = firstText(obj, "figureString", "figure", "figure_string");
            if (st.figure.isEmpty()) st.figure = item.figure;
            st.hotelKey = hotel;
            st.online = obj.optBoolean("online", optBoolAny(obj, false, "isOnline"));
            st.privateProfile = !optBoolAny(obj, true, "profileVisible", "isProfileVisible", "visible");
            st.lastAccess = firstText(obj, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
            return st;
        } catch(Exception ignored) {
            return null;
        }
    }

    private File favoriteHeadCacheDir() {
        File dir = new File(getCacheDir(), "favorite_heads");
        try { dir.mkdirs(); } catch(Exception ignored) {}
        return dir;
    }

    private File favoriteHeadCacheFile(String hotelKey, String nick) {
        return favoriteHeadCacheFile(hotelKey, nick, "");
    }

    private File favoriteHeadCacheFile(String hotelKey, String nick, String uniqueId) {
        String key = profileIdentityKey(hotelKey, uniqueId, nick);
        if (key == null || key.trim().isEmpty()) key = "unknown";
        return new File(favoriteHeadCacheDir(), Math.abs(key.hashCode()) + ".png");
    }

    private void deleteFavoriteHeadCache(String hotelKey, String nick) {
        try {
            File f = favoriteHeadCacheFile(hotelKey, nick);
            if (f.exists()) f.delete();
        } catch(Exception ignored) {}
    }

    private void cacheFavoriteHeadAsync(FavoriteStatus st) {
        if (st == null || st.nick == null || st.nick.trim().isEmpty()) return;
        executor.execute(() -> {
            try {
                Bitmap b = downloadFavoriteHeadBitmap(st);
                if (b != null) saveFavoriteHeadBitmap(st, b);
            } catch(Exception ignored) {}
        });
    }

    private Bitmap downloadFavoriteHeadBitmap(FavoriteStatus st) {
        HttpURLConnection c = null;
        try {
            if (st == null) return null;
            String url = "";
            if (st.nick != null && !st.nick.trim().isEmpty()) {
                url = avatarHeadByNameForHotel(st.nick, st.hotelKey);
            } else if (st.figure != null && !st.figure.trim().isEmpty()) {
                url = "https://" + hotelDomain(st.hotelKey) + "/habbo-imaging/avatarimage?figure=" + enc(st.figure) + "&size=m&direction=2&head_direction=2&headonly=1";
            }
            if (url.isEmpty()) return null;
            c = (HttpURLConnection)new URL(url).openConnection();
            c.setConnectTimeout(10000);
            c.setReadTimeout(15000);
            return BitmapFactory.decodeStream(c.getInputStream());
        } catch(Exception ignored) {
            return null;
        } finally {
            try { if (c != null) c.disconnect(); } catch(Exception ignored) {}
        }
    }

    private void saveFavoriteHeadBitmap(FavoriteStatus st, Bitmap bitmap) {
        if (st == null || bitmap == null) return;
        try {
            File f = favoriteHeadCacheFile(st.hotelKey, st.nick, st.uniqueId);
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch(Exception ignored) {}
    }

    private Bitmap loadFavoriteHeadFromCache(FavoriteStatus st) {
        try {
            if (st == null) return null;
            File f = favoriteHeadCacheFile(st.hotelKey, st.nick, st.uniqueId);
            if (f.exists()) return BitmapFactory.decodeFile(f.getAbsolutePath());
        } catch(Exception ignored) {}
        return null;
    }

    private Bitmap loadNotificationHeadBitmap(FavoriteStatus st) {
        try {
            if (st == null) return BitmapFactory.decodeResource(getResources(), R.drawable.pre_load_head);
            Bitmap fresh = downloadFavoriteHeadBitmap(st);
            if (fresh != null) {
                saveFavoriteHeadBitmap(st, fresh);
                return fresh;
            }
            Bitmap cached = loadFavoriteHeadFromCache(st);
            if (cached != null) return cached;
            return BitmapFactory.decodeResource(getResources(), R.drawable.pre_load_head);
        } catch(Exception ignored) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.pre_load_head);
        }
    }

    private void showFavoriteOnlineSystemNotification(FavoriteStatus st) {
        try {
            if (st == null) return;
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if (nm == null) return;
            String channelId = "favorite_online";
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel ch = new NotificationChannel(channelId, t(R.string.favorites), NotificationManager.IMPORTANCE_HIGH);
                nm.createNotificationChannel(ch);
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(this, 1207, intent, Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
            Bitmap largeIcon = loadNotificationHeadBitmap(st);
            Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, channelId) : new Notification.Builder(this);
            b.setSmallIcon(R.drawable.notification_image)
             .setContentTitle(t(R.string.favorites))
             .setContentText(tr(R.string.favorite_online_banner, st.nick))
             .setWhen(System.currentTimeMillis())
             .setShowWhen(true)
             .setPriority(Notification.PRIORITY_HIGH)
             .setContentIntent(pi)
             .setAutoCancel(true)
             .setStyle(new Notification.BigTextStyle().bigText(tr(R.string.favorite_online_banner, st.nick)));
            if (largeIcon != null) b.setLargeIcon(largeIcon);
            nm.notify(Math.abs(profileIdentityKey(st.hotelKey, st.uniqueId, st.nick).hashCode()), b.build());
        } catch(Exception ignored) {}
    }

    private void showFavoriteOnlineBanner(FavoriteStatus st) {
        if (screen == null || st == null) return;
        final LinearLayout banner = new LinearLayout(this);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setPadding(dp(12), dp(10), dp(12), dp(10));
        banner.setBackground(round(Color.argb(lightTheme ? 235 : 235, 24, 138, 85), dp(18), Color.argb(150, 120, 255, 190), 1));
        if (Build.VERSION.SDK_INT >= 21) banner.setElevation(dp(30));

        ImageView head = new ImageView(this);
        head.setScaleType(ImageView.ScaleType.FIT_CENTER);
        banner.addView(head, new LinearLayout.LayoutParams(dp(46), dp(46)));
        if (st.nick != null && !st.nick.isEmpty()) loadHeadImage(head, avatarHeadByNameForHotel(st.nick, st.hotelKey));
        else if (st.figure != null && !st.figure.isEmpty()) loadHeadImage(head, avatarHead(st.figure));

        TextView msg = habboText(tr(R.string.favorite_online_banner, st.nick), 14, true);
        msg.setTextColor(Color.WHITE);
        msg.setMaxLines(2);
        msg.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, -2, 1);
        mp.leftMargin = dp(10);
        banner.addView(msg, mp);

        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(-1, -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        bp.setMargins(dp(14), dp(18), dp(14), 0);
        banner.setTranslationY(-dp(90));
        banner.setAlpha(0f);
        screen.addView(banner, bp);
        banner.bringToFront();
        banner.animate().translationY(0).alpha(1f).setDuration(220).start();
        uiHandler.postDelayed(() -> {
            try {
                banner.animate().translationY(-dp(90)).alpha(0f).setDuration(220).withEndAction(() -> {
                    try { screen.removeView(banner); } catch(Exception ignored) {}
                }).start();
            } catch(Exception ignored) {}
        }, 4200L);
    }

    private void loadFavoriteProfiles() {
        favoriteProfiles.clear();
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_FAVORITES, "");
        if (raw == null || raw.trim().isEmpty()) return;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length() && favoriteProfiles.size() < MAX_FAVORITES; i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String nick = o.optString("nick", "").trim();
                if (nick.isEmpty()) continue;
                String hotel = normalizeHotelKey(o.optString("hotel", "br"));
                if (hotel.isEmpty()) hotel = "br";
                favoriteProfiles.add(new ProfileHistoryItem(nick, o.optString("figure", ""), hotel, o.optString("uniqueId", o.optString("id", ""))));
            }
        } catch(Exception ignored) {}
    }

    private void saveFavoriteProfiles() {
        JSONArray arr = new JSONArray();
        try {
            for (ProfileHistoryItem item : favoriteProfiles) {
                JSONObject o = new JSONObject();
                o.put("nick", item.nick);
                o.put("figure", item.figure);
                o.put("uniqueId", item.uniqueId);
                String hotel = normalizeHotelKey(item.hotelKey);
                o.put("hotel", hotel.isEmpty() ? "br" : hotel);
                arr.put(o);
            }
        } catch(Exception ignored) {}
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_FAVORITES, arr.toString()).apply();
    }

    private String favoriteKey(String hotelKey, String nick) { return normalizeHotelKey(hotelKey) + ":" + normalizeNickKey(nick); }

    private String profileIdentityKey(String hotelKey, String uniqueId, String nick) {
        String hotel = normalizeHotelKey(hotelKey);
        String id = normalizeNickKey(uniqueId);
        if (!id.isEmpty()) return hotel + ":id:" + id;
        return hotel + ":nick:" + normalizeNickKey(nick);
    }

    private String favoriteKey(ProfileHistoryItem item) {
        if (item == null) return "";
        return profileIdentityKey(item.hotelKey, item.uniqueId, item.nick);
    }

    private String favoriteKey(ProfileResult r) {
        if (r == null) return "";
        String hotel = normalizeHotelKey(r.hotelKey);
        if (hotel.isEmpty()) hotel = currentHotelKey;
        String nick = r.name == null || r.name.trim().isEmpty() ? r.searchedNick : r.name;
        return profileIdentityKey(hotel, r.uniqueId, nick);
    }

    private void applyFavoriteRowVisualState(LinearLayout row, ProfileHistoryItem item) {
        if (row == null || item == null) return;

        FavoriteStatus st = favoriteStatusCache.get(favoriteKey(item));
        boolean online = Boolean.TRUE.equals(favoriteOnlineStates.get(favoriteKey(item)));
        boolean privateProfile = false;

        if (st != null) {
            online = st.online;
            privateProfile = st.privateProfile;
        }

        int bgColor;
        int strokeColor;
        if (privateProfile) {
            bgColor = Color.rgb(10, 10, 14);
            strokeColor = lightTheme ? Color.rgb(54, 54, 64) : Color.argb(155, 110, 110, 125);
        } else if (online) {
            bgColor = lightTheme ? Color.rgb(241, 232, 252) : Color.argb(74, 139, 52, 217);
            strokeColor = lightTheme ? Color.rgb(139, 52, 217) : Color.argb(190, 171, 77, 255);
        } else {
            bgColor = lightTheme ? Color.rgb(232, 232, 236) : Color.rgb(42, 42, 50);
            strokeColor = lightTheme ? Color.rgb(192, 192, 198) : Color.argb(82, 180, 180, 190);
        }

        row.setBackground(round(bgColor, dp(16), strokeColor, 1));
        applyFavoriteRowTextColor(row, privateProfile);

        View head = row.findViewWithTag("favorite_head");
        if (head != null) {
            head.setAlpha((privateProfile || !online) ? 0.50f : 1.0f);
        }
    }

    private void applyFavoriteRowTextColor(View view, boolean privateProfile) {
        if (view == null) return;
        if (view instanceof TextView && privateProfile) {
            ((TextView)view).setTextColor(Color.WHITE);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i = 0; i < group.getChildCount(); i++) applyFavoriteRowTextColor(group.getChildAt(i), privateProfile);
        }
    }

    private int favoriteSortRank(ProfileHistoryItem item) {
        if (item == null) return 3;
        FavoriteStatus st = favoriteStatusCache.get(favoriteKey(item));
        if (st != null && st.privateProfile) return 2;
        boolean online = st != null ? st.online : Boolean.TRUE.equals(favoriteOnlineStates.get(favoriteKey(item)));
        return online ? 0 : 1;
    }

    private boolean isFavoriteProfile(ProfileResult r) {
        String key = favoriteKey(r);
        if (key.isEmpty()) return false;
        for (ProfileHistoryItem item : favoriteProfiles) if (favoriteKey(item).equals(key)) return true;
        return false;
    }

    private void toggleFavoriteProfile(ProfileResult r) {
        if (r == null) return;
        String hotel = normalizeHotelKey(r.hotelKey);
        if (hotel.isEmpty()) hotel = currentHotelKey;
        String nick = r.name == null || r.name.trim().isEmpty() ? r.searchedNick : r.name;
        if (nick == null || nick.trim().isEmpty()) return;
        String key = favoriteKey(r);
        for (int i = favoriteProfiles.size() - 1; i >= 0; i--) {
            ProfileHistoryItem item = favoriteProfiles.get(i);
            if (favoriteKey(item).equals(key)) {
                deleteFavoriteHeadCache(item.hotelKey, item.nick);
                favoriteOnlineStates.remove(key);
                favoriteProfiles.remove(i);
                saveFavoriteProfiles();
                updateFavoriteOnlineBadgeText();
                toast(t(R.string.favorite_removed));
                return;
            }
        }
        if (favoriteProfiles.size() >= MAX_FAVORITES) {
            toast(tr(R.string.favorite_limit_reached, MAX_FAVORITES));
            return;
        }
        favoriteProfiles.add(0, new ProfileHistoryItem(nick.trim(), r.figure, hotel, r.uniqueId));
        while (favoriteProfiles.size() > MAX_FAVORITES) favoriteProfiles.remove(favoriteProfiles.size() - 1);
        saveFavoriteProfiles();
        updateFavoriteOnlineBadgeText();
        toast(t(R.string.favorite_added));
    }

    private void showFavoriteProfilesDialog() {
        final Dialog dialog = new Dialog(this);
        PullDispatchFrameLayout full = new PullDispatchFrameLayout(this);
        full.setBackground(makeBg());

        LinearLayout favoritesPullIndicator = favoritesPullRefreshIndicator();
        full.addView(favoritesPullIndicator, favoritesPullIndicatorLayoutParams());

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(16), dp(34), dp(16), dp(82));
        wrap.setBackgroundColor(Color.TRANSPARENT);
        full.addView(wrap, new FrameLayout.LayoutParams(-1, -1));

        FrameLayout favoritesBottomNavigation = addBottomNavigation(full, 2, dialog);
        dialog.setContentView(full);
        applySafeAreaInsets(dialog.getWindow(), full);

        TextView title = habboText(t(R.string.favorites), 24, true);
        title.setGravity(Gravity.CENTER);
        wrap.addView(title, lp(-1, -2, 0, 0, 0, 18));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(true);
        sv.setScrollbarFadingEnabled(false);
        tintScrollBar(sv);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list, new ScrollView.LayoutParams(-1, -2));
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(-1, 0, 1);
        wrap.addView(sv, svLp);
        bindBottomNavigationAutoHide(sv, favoritesBottomNavigation);

        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            list.removeAllViews();
            if (favoriteProfiles.isEmpty()) {
                list.addView(centerNote(t(R.string.no_favorites)));
                return;
            }
            ArrayList<ProfileHistoryItem> sortedFavorites = new ArrayList<>(favoriteProfiles);
            Collections.sort(sortedFavorites, (a, b) -> {
                int ra = favoriteSortRank(a);
                int rb = favoriteSortRank(b);
                if (ra != rb) return Integer.compare(ra, rb);
                return String.valueOf(a.nick).compareToIgnoreCase(String.valueOf(b.nick));
            });
            for (ProfileHistoryItem item : sortedFavorites) list.addView(favoriteProfileRow(item, dialog, render[0]));
        };
        render[0].run();
        bindFavoritesPullRefresh(full, sv, wrap, favoritesPullIndicator, render[0]);

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            w.setWindowAnimations(0);
            w.setAttributes(params);
        }
    }

    private LinearLayout favoritesPullRefreshIndicator() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(16), dp(8), dp(16), dp(8));
        box.setAlpha(0f);
        box.setVisibility(View.GONE);
        box.setTranslationY(-dp(44));
        box.setBackground(round(lightTheme ? Color.WHITE : Color.rgb(34, 21, 54), dp(18), lightTheme ? Color.rgb(220,220,226) : Color.argb(58,255,255,255), 1));

        CircularPullProgressView progress = new CircularPullProgressView(this);
        progress.setProgressPct(0);
        progress.setTag("progress");
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(dp(36), dp(36));
        pp.gravity = Gravity.CENTER_HORIZONTAL;
        box.addView(progress, pp);

        TextView label = text(t(R.string.favorites_pull), 12, lightTheme ? Color.rgb(45,45,50) : Color.WHITE, true);
        label.setGravity(Gravity.CENTER);
        label.setTag("label");
        LinearLayout.LayoutParams lpLabel = new LinearLayout.LayoutParams(-2, -2);
        lpLabel.topMargin = dp(6);
        box.addView(label, lpLabel);
        return box;
    }

    private FrameLayout.LayoutParams favoritesPullIndicatorLayoutParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, dp(82), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        params.topMargin = dp(18);
        return params;
    }

    private void bindFavoritesPullRefresh(final PullDispatchFrameLayout touchHost, final ScrollView scrollView, final View elasticView, final LinearLayout indicator, final Runnable refreshRender) {
        if (touchHost == null || scrollView == null || elasticView == null || indicator == null) return;
        final float[] startY = {0f};
        final boolean[] tracking = {false};
        final boolean[] dragging = {false};
        final boolean[] ready = {false};
        final int trigger = dp(220);
        final int maxPull = dp(190);
        final CircularPullProgressView progress = indicator.findViewWithTag("progress");
        final TextView label = indicator.findViewWithTag("label");

        touchHost.setPullTouchListener(event -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                startY[0] = event.getRawY();
                tracking[0] = scrollView.getScrollY() <= 0;
                dragging[0] = false;
                ready[0] = false;
                return;
            }

            if (action == MotionEvent.ACTION_MOVE && tracking[0]) {
                float dy = event.getRawY() - startY[0];
                if (dy <= 0f) {
                    resetFavoritesPullIndicator(elasticView, indicator, progress, label);
                    dragging[0] = false;
                    ready[0] = false;
                    return;
                }
                if (scrollView.getScrollY() > 0 && !dragging[0]) return;

                dragging[0] = true;
                float progressValue = Math.max(0f, Math.min(1f, dy / Math.max(1, trigger)));
                float elastic = elasticPullDistance(dy, trigger, maxPull);
                elasticView.setTranslationY(elastic);
                indicator.setVisibility(View.VISIBLE);
                indicator.animate().cancel();
                indicator.setAlpha(Math.max(0.15f, progressValue));
                indicator.setTranslationY(-dp(22) + (elastic * 0.40f));
                if (progress != null) progress.setProgressPct(progressValue);
                if (label != null) label.setText(progressValue >= 1f ? t(R.string.favorites_release) : t(R.string.favorites_pull));
                ready[0] = progressValue >= 1f;
                return;
            }

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                boolean shouldRefresh = tracking[0] && dragging[0] && ready[0];
                tracking[0] = false;
                dragging[0] = false;
                ready[0] = false;
                resetFavoritesPullIndicator(elasticView, indicator, progress, label);
                if (shouldRefresh) refreshFavoritesFromPull(refreshRender);
            }
        });
    }

    private void resetFavoritesPullIndicator(View elasticView, LinearLayout indicator, CircularPullProgressView progress, TextView label) {
        if (progress != null) progress.setProgressPct(0);
        if (label != null) label.setText(t(R.string.favorites_pull));
        if (elasticView != null) elasticView.animate().translationY(0f).setDuration(190L).start();
        if (indicator != null) indicator.animate().alpha(0f).translationY(-dp(44)).setDuration(180L).withEndAction(() -> indicator.setVisibility(View.GONE)).start();
    }

    private void refreshFavoritesFromPull(Runnable refreshRender) {
        long now = System.currentTimeMillis();
        long wait = FAVORITES_REFRESH_COOLDOWN_MS - (now - lastFavoritesPullRefreshAt);
        if (wait > 0) {
            long seconds = Math.max(1L, (wait + 999L) / 1000L);
            toast(tr(R.string.wait_refresh, seconds));
            return;
        }
        lastFavoritesPullRefreshAt = now;
        toast(t(R.string.favorites_updating));
        executor.execute(() -> {
            ArrayList<ProfileHistoryItem> snapshot = new ArrayList<>(favoriteProfiles);
            for (ProfileHistoryItem item : snapshot) {
                try {
                    FavoriteStatus st = fetchFavoriteStatus(item);
                    if (st == null) continue;
                    String oldKey = favoriteKey(item);
                    String newKey = profileIdentityKey(st.hotelKey, st.uniqueId, st.nick);
                    cacheFavoriteHeadAsync(st);
                    favoriteOnlineStates.put(oldKey, st.online);
                    favoriteStatusCache.put(oldKey, st);
                    favoriteOnlineStates.put(newKey, st.online);
                    favoriteStatusCache.put(newKey, st);
                    setStoredFavoriteOnlineState(oldKey, st.online);
                    setStoredFavoriteOnlineState(newKey, st.online);
                } catch(Exception ignored) {}
            }
            runOnUiThread(() -> {
                if (refreshRender != null) refreshRender.run();
                updateFavoriteOnlineBadgeText();
                toast(t(R.string.favorites_updated));
            });
        });
    }

    private LinearLayout favoriteProfileRow(ProfileHistoryItem item, Dialog dialog, Runnable refresh) {
        LinearLayout row = profileListRowBase(item, true);
        applyFavoriteRowVisualState(row, item);

        TextView remove = text("", 18, Color.WHITE, true);
        remove.setGravity(Gravity.CENTER);
        remove.setBackground(new RemoveXDrawable());
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(dp(38), dp(38));
        rp.leftMargin = dp(6);
        row.addView(remove, rp);
        remove.setOnClickListener(v -> {
            for (int i = favoriteProfiles.size() - 1; i >= 0; i--) {
                ProfileHistoryItem f = favoriteProfiles.get(i);
                if (favoriteKey(f).equals(favoriteKey(item))) {
                    deleteFavoriteHeadCache(f.hotelKey, f.nick);
                    favoriteOnlineStates.remove(favoriteKey(f));
                    favoriteStatusCache.remove(favoriteKey(f));
                    favoriteProfiles.remove(i);
                }
            }
            saveFavoriteProfiles();
            updateFavoriteOnlineBadgeText();
            if (refresh != null) refresh.run();
        });
        bindProfileCardOpenAndHold(row, item.nick, item.hotelKey, item.figure, item.uniqueId, () -> {
            if (!isCurrentProfileListItem(item)) openProfileListItem(item, dialog);
        });

        updateFavoriteOnlineRowAsync(item, refresh);
        return row;
    }

    private void updateFavoriteOnlineRowAsync(ProfileHistoryItem item, Runnable refresh) {
        if (item == null) return;
        final String key = favoriteKey(item);
        executor.execute(() -> {
            FavoriteStatus st = fetchFavoriteStatus(item);
            if (st == null) return;
            cacheFavoriteHeadAsync(st);
            FavoriteStatus oldStatus = favoriteStatusCache.put(key, st);
            Boolean old = favoriteOnlineStates.put(key, st.online);
            setStoredFavoriteOnlineState(key, st.online);
            boolean changed = old == null || old.booleanValue() != st.online || oldStatus == null || oldStatus.privateProfile != st.privateProfile;
            if (changed) {
                runOnUiThread(() -> { if (refresh != null) refresh.run(); });
            }
        });
    }

    private boolean isCurrentProfileListItem(ProfileHistoryItem item) {
        if (item == null) return false;
        String itemHotel = normalizeHotelKey(item.hotelKey);
        String currentHotel = activeRenderedProfile != null ? normalizeHotelKey(activeRenderedProfile.hotelKey) : normalizeHotelKey(currentHotelKey);
        if (!itemHotel.equals(currentHotel)) return false;

        String currentId = activeRenderedProfile != null ? normalizeNickKey(activeRenderedProfile.uniqueId) : "";
        String itemId = normalizeNickKey(item.uniqueId);
        if (!currentId.isEmpty() && !itemId.isEmpty()) return currentId.equals(itemId);

        String currentNick = activeRenderedProfile != null && activeRenderedProfile.name != null && !activeRenderedProfile.name.trim().isEmpty() ? activeRenderedProfile.name : currentLoadedNick;
        return !currentNick.isEmpty() && normalizeNickKey(currentNick).equals(normalizeNickKey(item.nick));
    }

    private void openProfileListItem(ProfileHistoryItem item, Dialog dialog) {
        if (item == null) return;
        if (dialog != null) dialog.dismiss();
        currentHotelKey = normalizeHotelKey(item.hotelKey);
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_HOTEL, currentHotelKey).apply();
        currentLoadedNick = "";
        activeSearchToken++;
        searchInProgress = false;
        profileSectionsInProgress = false;
        inlineProgressPct = 0;
        inlineProgressMessage = "";
        rebuildUiPreservingProfile();
        openProfileReference(item.nick, item.uniqueId, item.figure, currentHotelKey);
    }

    private LinearLayout profileListRowBase(ProfileHistoryItem item) {
        return profileListRowBase(item, true);
    }

    private LinearLayout profileListRowBase(ProfileHistoryItem item, boolean showOnlineState) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(8), dp(10), dp(8));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(20,255,255,255), dp(16), lightTheme ? Color.rgb(218,218,218) : Color.argb(30,255,255,255), 1));
        row.setLayoutParams(lp(-1, dp(72), 0, 0, 0, 8));

        ImageView head = new ImageView(this);
        head.setTag("favorite_head");
        head.setScaleType(ImageView.ScaleType.FIT_CENTER);
        row.addView(head, new LinearLayout.LayoutParams(dp(54), dp(56)));
        if (showOnlineState) loadHeadImage(head, avatarHeadByNameForHotel(item.nick, item.hotelKey));
        else loadHeadImageForKnownProfile(head, item.figure, item.uniqueId, item.nick, item.hotelKey);


        LinearLayout mid = new LinearLayout(this);
        mid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, -2, 1);
        mp.leftMargin = dp(10);
        row.addView(mid, mp);
        TextView name = habboText(item.nick, 16, true);
        name.setMaxLines(1);
        name.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);
        nameRow.addView(name, new LinearLayout.LayoutParams(-2, -2));
        if (showOnlineState && Boolean.TRUE.equals(favoriteOnlineStates.get(favoriteKey(item)))) {
            TextView online = text(t(R.string.favorite_currently_online), 12, purple, true);
            online.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(-2, -2);
            op.leftMargin = dp(8);
            nameRow.addView(online, op);
        }
        mid.addView(nameRow);

        LinearLayout hotelLine = new LinearLayout(this);
        hotelLine.setGravity(Gravity.CENTER_VERTICAL);
        ImageView flag = new ImageView(this);
        flag.setImageDrawable(new HotelFlagDrawable(item.hotelKey));
        hotelLine.addView(flag, new LinearLayout.LayoutParams(dp(24), dp(16)));
        mid.addView(hotelLine, new LinearLayout.LayoutParams(-1, -2));
        return row;
    }


    private void bindProfileHeadPreviewHold(final View target, final String nick, final String hotelKey, final String fallbackFigure) {
        bindProfileHeadPreviewHold(target, nick, hotelKey, fallbackFigure, "", null);
    }

    private void bindProfileHeadPreviewHold(final View target, final String nick, final String hotelKey, final String fallbackFigure, final String uniqueId) {
        bindProfileHeadPreviewHold(target, nick, hotelKey, fallbackFigure, uniqueId, null);
    }

    private void bindProfileHeadPreviewHold(final View target, final String nick, final String hotelKey, final String fallbackFigure, final String uniqueId, final Runnable openAction) {
        bindProfileCardOpenAndHold(target, nick, hotelKey, fallbackFigure, uniqueId, openAction);
    }

    private void bindProfileCardOpenAndHold(final View target, final String nick, final String hotelKey, final String fallbackFigure, final Runnable openAction) {
        bindProfileCardOpenAndHold(target, nick, hotelKey, fallbackFigure, "", openAction);
    }

    private void bindProfileCardOpenAndHold(final View target, final String nick, final String hotelKey, final String fallbackFigure, final String uniqueId, final Runnable openAction) {
        if (target == null) return;
        target.setClickable(true);
        final Runnable[] holdTask = new Runnable[1];
        final boolean[] fired = {false};
        final boolean[] movedBeyondClick = {false};
        final float[] downX = {0f};
        final float[] downY = {0f};
        target.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                downX[0] = event.getX();
                downY[0] = event.getY();
                fired[0] = false;
                movedBeyondClick[0] = false;
                if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                holdTask[0] = () -> {
                    fired[0] = true;
                    showMiniProfilePreviewDialog(nick, hotelKey, fallbackFigure, uniqueId);
                };
                uiHandler.postDelayed(holdTask[0], 500L);
                return true;
            }
            if (action == MotionEvent.ACTION_MOVE) {
                float dx = Math.abs(event.getX() - downX[0]);
                float dy = Math.abs(event.getY() - downY[0]);
                if (dx > dp(12) || dy > dp(12)) {
                    movedBeyondClick[0] = true;
                    if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                }
                return true;
            }
            if (action == MotionEvent.ACTION_UP) {
                if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                if (!fired[0] && !movedBeyondClick[0] && openAction != null) openAction.run();
                return true;
            }
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE) {
                if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                return true;
            }
            return true;
        });
    }

    private void showMiniProfilePreviewDialog(final String nick, final String hotelKey, final String fallbackFigure, final String uniqueId) {
        final Dialog dialog = new Dialog(this);
        LinearLayout rootDialog = new LinearLayout(this);
        rootDialog.setOrientation(LinearLayout.VERTICAL);
        rootDialog.setPadding(dp(18), dp(18), dp(18), dp(18));
        rootDialog.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        dialog.setContentView(rootDialog);
        applySafeAreaInsets(dialog.getWindow(), rootDialog);

        FrameLayout avatarWrap = new FrameLayout(this);
        avatarWrap.setPadding(dp(8), dp(2), dp(8), dp(2));
        rootDialog.addView(avatarWrap, lp(-1, dp(190), 0, 0, 0, 10));

        ImageView avatar = new ImageView(this);
        avatar.setAdjustViewBounds(true);
        avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        avatar.setPadding(dp(44), 0, dp(44), 0);
        avatarWrap.addView(avatar, new FrameLayout.LayoutParams(-1, -1));
        String initialFigure = fallbackFigure == null ? "" : fallbackFigure.trim();
        avatar.setImageResource(R.drawable.pre_load);
        if (!initialFigure.isEmpty()) loadAvatarImage(avatar, avatarFull(initialFigure, 2));

        TextView favoriteBtn = text("", 22, Color.WHITE, true);
        favoriteBtn.setGravity(Gravity.CENTER);
        favoriteBtn.setPadding(0, 0, 0, 0);
        ProfileResult initialProfile = miniProfileResult(nick, initialFigure, hotelKey, uniqueId);
        favoriteBtn.setBackground(new FavoriteStarDrawable(isFavoriteProfile(initialProfile)));
        FrameLayout.LayoutParams favLp = new FrameLayout.LayoutParams(dp(42), dp(42), Gravity.TOP | Gravity.RIGHT);
        favLp.topMargin = dp(8);
        favLp.rightMargin = dp(8);
        avatarWrap.addView(favoriteBtn, favLp);

        TextView name = habboText(nick == null || nick.trim().isEmpty() ? t(R.string.profile) : nick.trim(), 24, true);
        name.setGravity(Gravity.CENTER);
        rootDialog.addView(name, lp(-1, -2, 0, 0, 0, 8));

        ProgressBar miniLoader = new ProgressBar(this);
        miniLoader.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= 21) miniLoader.setIndeterminateTintList(ColorStateList.valueOf(purple));
        LinearLayout loaderLine = new LinearLayout(this);
        loaderLine.setGravity(Gravity.CENTER);
        loaderLine.addView(miniLoader, new LinearLayout.LayoutParams(dp(30), dp(30)));
        rootDialog.addView(loaderLine, lp(-1, dp(38), 0, 0, 0, 10));

        TextView motto = habboText("", 15, false);
        motto.setGravity(Gravity.CENTER);
        motto.setTextColor(lightTheme ? Color.rgb(70,70,70) : Color.argb(220,255,255,255));
        motto.setMaxLines(3);
        motto.setEllipsize(TextUtils.TruncateAt.END);
        motto.setVisibility(View.GONE);
        rootDialog.addView(motto, lp(-1, -2, 0, 0, 0, 10));

        LinearLayout badges = new LinearLayout(this);
        badges.setGravity(Gravity.CENTER);
        badges.setOrientation(LinearLayout.HORIZONTAL);
        rootDialog.addView(badges, lp(-1, -2, 0, 0, 0, 10));

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.VERTICAL);
        rootDialog.addView(stats, lp(-1, -2, 0, 0, 0, 12));

        stats.addView(miniStatRow("status_offline", t(R.string.status), "", "", false));
        stats.addView(miniStatRow("clock", t(R.string.last_login), "", "", false));
        stats.addView(miniStatRow("calendar", t(R.string.creation), "", "", false));

        final MiniProfilePreview[] loaded = new MiniProfilePreview[1];

        TextView openFull = dialogButton(t(R.string.open_full_profile));
        openFull.setBackground(grad(dp(14), purple2, purple));
        rootDialog.addView(openFull, lp(-1, dp(48), 0, 0, 0, 0));
        openFull.setOnClickListener(v -> {
            dialog.dismiss();
            MiniProfilePreview data = loaded[0];
            String openNick = data != null && data.nick != null && !data.nick.trim().isEmpty() ? data.nick : nick;
            String openId = data != null && data.uniqueId != null && !data.uniqueId.trim().isEmpty() ? data.uniqueId : uniqueId;
            String openFig = data != null && data.figure != null && !data.figure.trim().isEmpty() ? data.figure : fallbackFigure;
            openMiniProfileFull(openNick, hotelKey, openId, openFig);
        });

        favoriteBtn.setOnClickListener(v -> {
            MiniProfilePreview data = loaded[0];
            String favNick = data != null && data.nick != null && !data.nick.trim().isEmpty() ? data.nick : nick;
            String favFig = data != null && data.figure != null && !data.figure.trim().isEmpty() ? data.figure : fallbackFigure;
            String favHotel = data != null && data.hotelKey != null && !data.hotelKey.trim().isEmpty() ? data.hotelKey : hotelKey;
            String favId = data != null && data.uniqueId != null && !data.uniqueId.trim().isEmpty() ? data.uniqueId : uniqueId;
            ProfileResult pr = miniProfileResult(favNick, favFig, favHotel, favId);
            toggleFavoriteProfile(pr);
            favoriteBtn.setBackground(new FavoriteStarDrawable(isFavoriteProfile(pr)));
        });

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(28), dp(430));
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(params);
        }

        executor.execute(() -> {
            MiniProfilePreview data = fetchMiniProfilePreview(nick, hotelKey, fallbackFigure, uniqueId);
            runOnUiThread(() -> {
                try {
                    if (!dialog.isShowing() || data == null) return;
                    loaded[0] = data;
                    if (data.figure != null && !data.figure.trim().isEmpty()) loadAvatarImage(avatar, avatarFull(data.figure, 2));
                    loaderLine.setVisibility(View.GONE);
                    name.setText(data.nick == null || data.nick.trim().isEmpty() ? nick : data.nick);
                    String mission = data.motto == null ? "" : data.motto.trim();
                    if (mission.isEmpty()) {
                        motto.setText("");
                        motto.setVisibility(View.GONE);
                    } else {
                        motto.setText(mission);
                        motto.setVisibility(View.VISIBLE);
                    }

                    ProfileResult pr = miniProfileResult(data.nick, data.figure, data.hotelKey, data.uniqueId);
                    favoriteBtn.setBackground(new FavoriteStarDrawable(isFavoriteProfile(pr)));

                    badges.removeAllViews();
                    if (data.banned) {
                        badges.addView(profileBadge(t(R.string.profile_banned), "ban", red));
                    } else if (data.privateProfile) {
                        badges.addView(profileBadge(t(R.string.profile_private), "lock", red));
                    }

                    boolean redBorder = data.privateProfile || data.banned;
                    rootDialog.setBackground(round(
                            dialogFillColor(),
                            dp(22),
                            redBorder ? Color.argb(150, 248, 82, 82) : dialogStrokeColor(),
                            redBorder ? 2 : 1
                    ));
                    stats.removeAllViews();
                    stats.addView(miniStatRow(data.online ? "status_online" : "status_offline", t(R.string.status), data.online ? t(R.string.online) : t(R.string.offline), "", redBorder));
                    stats.addView(miniStatRow("clock", t(R.string.last_login), niceDate(data.lastAccess), timeAgoText(data.lastAccess), redBorder));
                    stats.addView(miniStatRow("calendar", t(R.string.creation), niceDateOnly(data.memberSince), timeAgoText(data.memberSince), redBorder));
                } catch(Exception ignored) {}
            });
        });
    }

    private LinearLayout miniStatRow(String icon, String label, String value, String tooltip, boolean redBorder) {
        LinearLayout row = card(dp(18));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(7), dp(10), dp(7));
        row.setLayoutParams(lp(-1, dp(54), 0, 0, 0, 7));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(20,255,255,255), dp(18), redBorder ? Color.argb(130, 255, 64, 64) : (lightTheme ? Color.rgb(218,218,218) : Color.argb(30,255,255,255)), 1));

        if ("status".equals(icon) || "status_online".equals(icon) || "status_offline".equals(icon)) {
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            row.addView(iv, new LinearLayout.LayoutParams(dp(20), dp(20)));
            boolean onlineStatusIcon = "status_online".equals(icon) || (value != null && value.trim().equalsIgnoreCase(t(R.string.online)));
            Glide.with(this).asGif().load(onlineStatusIcon ? R.drawable.online : R.drawable.offline).into(iv);
        } else {
            IconView iv = new IconView(this, icon);
            row.addView(iv, new LinearLayout.LayoutParams(dp(18), dp(18)));
        }

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.leftMargin = dp(9);
        row.addView(texts, tp);
        texts.addView(text(label, 11, Color.argb(190,255,255,255), false));
        texts.addView(text(value == null || value.isEmpty() || "null".equalsIgnoreCase(value) ? "" : value, 14, Color.WHITE, true));

        if (tooltip != null && !tooltip.trim().isEmpty() && !"—".equals(tooltip.trim())) {
            row.setOnClickListener(v -> toast(tooltip));
        }
        return row;
    }

    private ProfileResult miniProfileResult(String nick, String figure, String hotelKey) {
        return miniProfileResult(nick, figure, hotelKey, "");
    }

    private ProfileResult miniProfileResult(String nick, String figure, String hotelKey, String uniqueId) {
        ProfileResult pr = new ProfileResult();
        pr.name = nick == null ? "" : nick.trim();
        pr.figure = figure == null ? "" : figure.trim();
        pr.uniqueId = uniqueId == null ? "" : uniqueId.trim();
        pr.hotelKey = normalizeHotelKey(hotelKey);
        if (pr.hotelKey.isEmpty()) pr.hotelKey = currentHotelKey;
        return pr;
    }

    private void openMiniProfileFull(String nick, String hotelKey, String uniqueId, String figure) {
        currentHotelKey = normalizeHotelKey(hotelKey);
        if (currentHotelKey.isEmpty()) currentHotelKey = defaultHotelForDeviceLocale();
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_HOTEL, currentHotelKey).apply();
        currentLoadedNick = "";
        activeSearchToken++;
        searchInProgress = false;
        profileSectionsInProgress = false;
        inlineProgressPct = 0;
        inlineProgressMessage = "";
        rebuildUiPreservingProfile();
        openProfileReference(nick == null ? "" : nick.trim(), uniqueId, figure, currentHotelKey);
    }

    private MiniProfilePreview fetchMiniProfilePreview(String nick, String hotelKey, String fallbackFigure, String uniqueId) {
        MiniProfilePreview out = new MiniProfilePreview();
        out.nick = nick == null ? "" : nick.trim();
        out.figure = fallbackFigure == null ? "" : fallbackFigure.trim();
        out.uniqueId = uniqueId == null ? "" : uniqueId.trim();
        out.hotelKey = normalizeHotelKey(hotelKey);
        if (out.hotelKey.isEmpty()) out.hotelKey = currentHotelKey;
        try {
            String lookupName = out.nick;
            JSONObject publicObj = lookupName.isEmpty()
                    ? null
                    : validProfileObject(tryJson("https://" + hotelDomain(out.hotelKey) + "/api/public/users?name=" + enc(lookupName)));
            if (publicObj != null && !out.uniqueId.isEmpty() && !isSameProfileId(out.uniqueId, publicObj)) {
                publicObj = null;
            }
            JSONObject officialUser = publicObj == null && !out.uniqueId.isEmpty()
                    ? validProfileObject(tryJson(
                            "https://" + hotelDomain(out.hotelKey) + "/api/public/users/" + enc(out.uniqueId)
                    ))
                    : null;
            JSONObject complement = null;
            JSONObject base = firstObject(validProfileObject(publicObj), validProfileObject(officialUser));
            if (base == null) {
                complement = !out.uniqueId.isEmpty()
                        ? fetchDirectHabbodexProfile(out.uniqueId)
                        : resolveHabbodexProfileFromSuggestions(
                                fetchHabbodexSuggestions(out.nick, out.hotelKey),
                                out.nick
                        );
                base = complement;
            }
            if (base == null) return out;

            String realId = firstText(base, "uniqueId", "id", "habboId");
            if (!realId.isEmpty()) out.uniqueId = realId;

            String realNick = firstText(base, "name", "username", "habboName");
            if (!realNick.isEmpty()) out.nick = realNick;

            String fig = firstText(base, "figureString", "figure", "figure_string");
            if (fig.isEmpty() && publicObj != null) fig = firstText(publicObj, "figureString", "figure", "figure_string");
            if (!fig.isEmpty()) out.figure = fig;

            out.motto = firstText(base, "motto", "mission");
            if (out.motto.isEmpty() && publicObj != null) out.motto = firstText(publicObj, "motto", "mission");

            out.online = optBoolAny(base, false, "online", "isOnline");
            if (publicObj != null && publicObj.has("online")) out.online = publicObj.optBoolean("online", out.online);

            out.privateProfile = !optBoolAny(base, true, "profileVisible", "isProfileVisible", "visible");
            if (publicObj != null && publicObj.has("profileVisible")) out.privateProfile = !publicObj.optBoolean("profileVisible", true);
            out.banned = publicObj == null
                    && officialUser == null
                    && resolveBannedFromHistoricalData(complement);
            out.privateProfile = out.banned
                    ? false
                    : resolveProfilePrivate(publicObj, officialUser, complement, null);
            if (out.banned) out.online = false;

            out.memberSince = firstText(base, "memberSince", "creationTime", "createdAt", "registeredAt", "created_at", "registerDate", "registrationDate");
            if (out.memberSince.isEmpty() && publicObj != null) out.memberSince = firstText(publicObj, "memberSince", "creationTime", "createdAt", "registeredAt", "created_at", "registerDate", "registrationDate");

            out.lastAccess = firstText(base, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
            if (out.lastAccess.isEmpty() && publicObj != null) out.lastAccess = firstText(publicObj, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
        } catch(Exception ignored) {}
        return out;
    }


    private static long parseHabboTimestampMsStatic(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) return 0L;
        ArrayList<String> candidates = new ArrayList<>();
        candidates.add(value);
        if (value.endsWith("Z")) candidates.add(value.substring(0, value.length() - 1) + "+0000");
        if (value.matches(".*[+-]\\d{2}:\\d{2}$")) {
            candidates.add(value.substring(0, value.length() - 3) + value.substring(value.length() - 2));
        }
        String[] patterns = new String[] {
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd",
                "dd/MM/yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm",
                "dd/MM/yyyy",
                "dd-MM-yyyy"
        };
        for (String candidate : candidates) {
            for (String pattern : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                    sdf.setLenient(false);
                    if (!pattern.endsWith("Z")) sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date d = sdf.parse(candidate);
                    if (d != null) return d.getTime();
                } catch(Exception ignored) {}
            }
        }
        return 0L;
    }

    private static boolean isFavoriteRecentlyOnlineStatic(FavoriteStatus st) {
        if (st == null || st.lastAccess == null || st.lastAccess.trim().isEmpty()) return false;
        long access = parseHabboTimestampMsStatic(st.lastAccess);
        if (access <= 0L) return false;
        long age = System.currentTimeMillis() - access;
        return age >= -30_000L && age <= 3L * 60L * 1000L;
    }

    private static class MiniProfilePreview {
        String nick = "", figure = "", uniqueId = "", hotelKey = "br", motto = "", lastAccess = "", memberSince = "";
        boolean online = false, privateProfile = false, banned = false;
    }

    private static class FavoriteStatus {
        String nick = "", figure = "", hotelKey = "br", uniqueId = "", lastAccess = "";
        boolean online = false, privateProfile = false;
    }

    private static class ProfileHistoryItem {
        final String nick;
        final String figure;
        final String hotelKey;
        final String uniqueId;
        ProfileHistoryItem(String nick, String figure, String hotelKey) {
            this(nick, figure, hotelKey, "");
        }
        ProfileHistoryItem(String nick, String figure, String hotelKey, String uniqueId) {
            this.nick = nick == null ? "" : nick;
            this.figure = figure == null ? "" : figure;
            this.hotelKey = hotelKey == null || hotelKey.trim().isEmpty() ? "br" : hotelKey;
            this.uniqueId = uniqueId == null ? "" : uniqueId.trim();
        }
    }

    private static class CachedJsonResponse {
        final String body;
        final long storedAtMs;

        CachedJsonResponse(String body, long storedAtMs) {
            this.body = body == null ? "" : body;
            this.storedAtMs = storedAtMs;
        }
    }

    private static class ProfileResult {
        String searchedNick = "", uniqueId = "", name = "", motto = "", figure = "", memberSince = "", lastAccess = "", level = "", starGems = "", totalBadges = "", hotelKey = "br";
        boolean online = false, privateProfile = false, banned = false;
        JSONObject habboPublic, dex, suggest, dexProfile, officialProfile;
        HashMap<String, JSONObject> officialBadgeLookup = new HashMap<>();
        ArrayList<JSONObject> previousNames = new ArrayList<>(), previousMottos = new ArrayList<>(), previousStyles = new ArrayList<>(), photos = new ArrayList<>(), friends = new ArrayList<>(), oldFriends = new ArrayList<>(), rooms = new ArrayList<>(), oldRooms = new ArrayList<>(), groups = new ArrayList<>(), selectedBadges = new ArrayList<>(), badges = new ArrayList<>(), badgesWithAchievements = new ArrayList<>();
        ArrayList<JSONObject> allPhotosSource = new ArrayList<>(), allStylesSource = new ArrayList<>();
        int photosNextPage = 0, stylesNextPage = 0, photosTotal = 0, stylesTotal = 0;
        int stylesRemoteNextPage = 0;
        int removedFriendsNextPage = 0, removedFriendsTotal = 0, friendsNextPage = 0, friendsTotal = 0, friendsTabPage = 1;
        int previousMottosSlideIndex = 0;
        int badgesNextPage = 0, badgesTotal = 0, badgesTabPage = 1;
        boolean photosHasMore = false, stylesHasMore = false, photosLoading = false, stylesLoading = false;
        boolean removedFriendsHasMore = false, removedFriendsLoading = false, friendsHasMore = false, friendsLoading = false, friendsPagedMode = false, friendsTabShowingRemoved = false, friendsTabSelectionTouched = false;
        boolean badgesHasMore = false, badgesLoading = false, badgesPagedMode = false, hideAchievementBadges = true;
        boolean officialProfileAttempted = false, officialPhotosAttempted = false, officialPhotosSucceeded = false, photosFromOfficial = false, stylesFromComplement = false, stylesRemotePaged = false, friendsDatesReady = false;
    }

    private static class ProfileSectionPayload {
        final String kind;
        final JSONObject object;
        final ArrayList<JSONObject> items;
        final boolean success;

        private ProfileSectionPayload(
                String kind,
                JSONObject object,
                ArrayList<JSONObject> items,
                boolean success
        ) {
            this.kind = kind == null ? "" : kind;
            this.object = object;
            this.items = items == null ? new ArrayList<>() : items;
            this.success = success;
        }

        static ProfileSectionPayload object(String kind, JSONObject value) {
            return new ProfileSectionPayload(kind, value, null, value != null);
        }

        static ProfileSectionPayload list(
                String kind,
                ArrayList<JSONObject> value,
                boolean success
        ) {
            return new ProfileSectionPayload(kind, null, value, success);
        }
    }

    private static class PageResult {
        ArrayList<JSONObject> items = new ArrayList<>();
        int page = 1, nextPage = 0, total = 0;
        boolean hasMore = false, success = false;
    }




    public static class FavoriteOnlineReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            final PendingResult pending = goAsync();
            new Thread(() -> {
                try {
                    checkFavoritesInBackground(context);
                } catch(Exception ignored) {
                } finally {
                    try { pending.finish(); } catch(Exception ignored) {}
                }
            }).start();
        }

        private static void checkFavoritesInBackground(Context context) {
            if (context == null) return;
            SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            if (!sp.getBoolean(PREF_NOTIFY_FAVORITE_ONLINE, false)) return;

            String rawFavorites = sp.getString(PREF_FAVORITES, "");
            if (rawFavorites == null || rawFavorites.trim().isEmpty()) return;

            JSONObject states;
            try {
                String rawStates = sp.getString(PREF_FAVORITE_ONLINE_STATES, "{}");
                states = new JSONObject(rawStates == null || rawStates.trim().isEmpty() ? "{}" : rawStates);
            } catch(Exception e) {
                states = new JSONObject();
            }

            try {
                JSONArray arr = new JSONArray(rawFavorites);
                for (int i=0; i<arr.length(); i++) {
                    JSONObject fav = arr.optJSONObject(i);
                    if (fav == null) continue;
                    String nick = fav.optString("nick", "").trim();
                    if (nick.isEmpty()) continue;
                    String hotel = normalizeHotelKeyStatic(fav.optString("hotel", "br"));
                    if (hotel.isEmpty()) hotel = "br";
                    String uniqueId = fav.optString("uniqueId", fav.optString("id", "")).trim();
                    String key = profileIdentityKeyStatic(hotel, uniqueId, nick);

                    FavoriteStatus st = fetchFavoriteStatusStatic(nick, fav.optString("figure", ""), hotel, uniqueId);
                    if (st == null) continue;

                    boolean hadPrevious = states.has(key);
                    boolean wasOnline = states.optBoolean(key, false);
                    states.put(key, st.online);

                    if (hadPrevious && !wasOnline && st.online && isFavoriteRecentlyOnlineStatic(st)) {
                        showFavoriteOnlineSystemNotificationStatic(context, st);
                    }
                }
                sp.edit().putString(PREF_FAVORITE_ONLINE_STATES, states.toString()).apply();
            } catch(Exception ignored) {}
        }

        private static FavoriteStatus fetchFavoriteStatusStatic(String nick, String fallbackFigure, String hotel, String uniqueId) {
            HttpURLConnection c = null;
            try {
                String safeId = uniqueId == null ? "" : uniqueId.trim();
                URL u;
                if (!safeId.isEmpty()) u = new URL("https://" + hotelDomainStatic(hotel) + "/api/public/users/" + URLEncoder.encode(safeId, "UTF-8"));
                else u = new URL("https://" + hotelDomainStatic(hotel) + "/api/public/users?name=" + URLEncoder.encode(nick, "UTF-8"));
                c = (HttpURLConnection)u.openConnection();
                c.setUseCaches(false);
                c.setDefaultUseCaches(false);
                c.setConnectTimeout(12000);
                c.setReadTimeout(18000);
                c.setRequestProperty("Accept", "application/json, text/plain, */*");
                c.setRequestProperty("Cache-Control", "no-cache, no-store");
                c.setRequestProperty("Pragma", "no-cache");
                c.setRequestProperty("User-Agent", "ToxicSearchTool/" + APP_VERSION + " Android");
                int code = c.getResponseCode();
                InputStream is = code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream();
                String body = readAllStatic(is);
                if (body == null || body.trim().isEmpty() || body.trim().startsWith("[")) return null;
                JSONObject root = new JSONObject(body);
                JSONObject obj = root.optJSONObject("user");
                if (obj == null) obj = root;
                if (obj.has("ok") && !obj.optBoolean("ok", true) && !obj.has("uniqueId")) return null;

                FavoriteStatus st = new FavoriteStatus();
                st.nick = obj.optString("name", nick);
                if (st.nick == null || st.nick.trim().isEmpty()) st.nick = nick;
                st.uniqueId = obj.optString("uniqueId", obj.optString("id", safeId));
                st.figure = obj.optString("figureString", obj.optString("figure", fallbackFigure == null ? "" : fallbackFigure));
                st.hotelKey = hotel;
                st.online = obj.optBoolean("online", obj.optBoolean("isOnline", false));
                st.privateProfile = !obj.optBoolean("profileVisible", obj.optBoolean("isProfileVisible", obj.optBoolean("visible", true)));
                st.lastAccess = obj.optString("lastAccessTime", obj.optString("lastLoginTime", obj.optString("lastOnline", obj.optString("lastVisit", ""))));
                return st;
            } catch(Exception ignored) {
                return null;
            } finally {
                try { if (c != null) c.disconnect(); } catch(Exception ignored) {}
            }
        }


        private static File favoriteHeadCacheDirStatic(Context context) {
            File dir = new File(context.getCacheDir(), "favorite_heads");
            try { dir.mkdirs(); } catch(Exception ignored) {}
            return dir;
        }

        private static File favoriteHeadCacheFileStatic(Context context, String hotelKey, String nick) {
            return favoriteHeadCacheFileStatic(context, hotelKey, nick, "");
        }

        private static File favoriteHeadCacheFileStatic(Context context, String hotelKey, String nick, String uniqueId) {
            String key = profileIdentityKeyStatic(hotelKey, uniqueId, nick);
            return new File(favoriteHeadCacheDirStatic(context), Math.abs(key.hashCode()) + ".png");
        }

        private static void saveFavoriteHeadBitmapStatic(Context context, FavoriteStatus st, Bitmap bitmap) {
            if (context == null || st == null || bitmap == null) return;
            try {
                FileOutputStream out = new FileOutputStream(favoriteHeadCacheFileStatic(context, st.hotelKey, st.nick, st.uniqueId));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch(Exception ignored) {}
        }

        private static Bitmap loadFavoriteHeadFromCacheStatic(Context context, FavoriteStatus st) {
            try {
                if (context == null || st == null) return null;
                File f = favoriteHeadCacheFileStatic(context, st.hotelKey, st.nick, st.uniqueId);
                if (f.exists()) return BitmapFactory.decodeFile(f.getAbsolutePath());
            } catch(Exception ignored) {}
            return null;
        }

        private static Bitmap loadNotificationHeadBitmapStatic(Context context, FavoriteStatus st) {
            HttpURLConnection c = null;
            try {
                if (context == null || st == null) return null;
                String url;
                if (st.nick != null && !st.nick.trim().isEmpty()) {
                    url = "https://" + hotelDomainStatic(st.hotelKey) + "/habbo-imaging/avatarimage?user=" + URLEncoder.encode(st.nick, "UTF-8") + "&size=m&direction=2&head_direction=2&headonly=1";
                } else if (st.figure != null && !st.figure.trim().isEmpty()) {
                    url = "https://" + hotelDomainStatic(st.hotelKey) + "/habbo-imaging/avatarimage?figure=" + URLEncoder.encode(st.figure, "UTF-8") + "&size=m&direction=2&head_direction=2&headonly=1";
                } else {
                    Bitmap cached = loadFavoriteHeadFromCacheStatic(context, st);
                    return cached != null ? cached : BitmapFactory.decodeResource(context.getResources(), R.drawable.pre_load_head);
                }
                c = (HttpURLConnection)new URL(url).openConnection();
                c.setConnectTimeout(10000);
                c.setReadTimeout(15000);
                Bitmap b = BitmapFactory.decodeStream(c.getInputStream());
                if (b != null) {
                    saveFavoriteHeadBitmapStatic(context, st, b);
                    return b;
                }
                Bitmap cached = loadFavoriteHeadFromCacheStatic(context, st);
                return cached != null ? cached : BitmapFactory.decodeResource(context.getResources(), R.drawable.pre_load_head);
            } catch(Exception ignored) {
                Bitmap cached = loadFavoriteHeadFromCacheStatic(context, st);
                return cached != null ? cached : (context == null ? null : BitmapFactory.decodeResource(context.getResources(), R.drawable.pre_load_head));
            } finally {
                try { if (c != null) c.disconnect(); } catch(Exception ignored) {}
            }
        }

        private static void showFavoriteOnlineSystemNotificationStatic(Context context, FavoriteStatus st) {
            try {
                NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm == null || st == null) return;
                String channelId = "favorite_online";
                if (Build.VERSION.SDK_INT >= 26) {
                    NotificationChannel ch = new NotificationChannel(channelId, localizedStringStatic(context, st.hotelKey, R.string.favorites), NotificationManager.IMPORTANCE_HIGH);
                    nm.createNotificationChannel(ch);
                }
                Intent open = new Intent(context, MainActivity.class);
                open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pi = PendingIntent.getActivity(context, 1207, open, Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
                String msg = localizedStringStatic(context, st.hotelKey, R.string.favorite_online_banner, st.nick == null ? "" : st.nick);
                Bitmap largeIcon = loadNotificationHeadBitmapStatic(context, st);
                Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, channelId) : new Notification.Builder(context);
                b.setSmallIcon(R.drawable.notification_image)
                 .setContentTitle(localizedStringStatic(context, st.hotelKey, R.string.favorites))
                 .setContentText(msg)
                 .setWhen(System.currentTimeMillis())
                 .setShowWhen(true)
                 .setPriority(Notification.PRIORITY_HIGH)
                 .setContentIntent(pi)
                 .setAutoCancel(true)
                 .setStyle(new Notification.BigTextStyle().bigText(msg));
                if (largeIcon != null) b.setLargeIcon(largeIcon);
                nm.notify(Math.abs(profileIdentityKeyStatic(st.hotelKey, st.uniqueId, st.nick).hashCode()), b.build());
            } catch(Exception ignored) {}
        }

        private static String readAllStatic(InputStream is) throws IOException {
            if (is == null) return "";
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
            return out.toString("UTF-8");
        }

        private static String normalizeHotelKeyStatic(String hotel) {
            String h = hotel == null ? "" : hotel.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
            if ("us".equals(h)) h = "com";
            String[] allowed = {"br","com","es","de","fr","fi","it","nl","tr"};
            for (String a : allowed) if (a.equals(h)) return h;
            return "";
        }

        private static String profileIdentityKeyStatic(String hotelKey, String uniqueId, String nick) {
            String hotel = normalizeHotelKeyStatic(hotelKey);
            String id = normalizeNickKeyStatic(uniqueId);
            if (!id.isEmpty()) return hotel + ":id:" + id;
            return hotel + ":nick:" + normalizeNickKeyStatic(nick);
        }

        private static String normalizeNickKeyStatic(String raw) {
            return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        }

        private static String hotelDomainStatic(String key) {
            String h = normalizeHotelKeyStatic(key);
            if ("com".equals(h)) return "www.habbo.com";
            if ("es".equals(h)) return "www.habbo.es";
            if ("de".equals(h)) return "www.habbo.de";
            if ("fr".equals(h)) return "www.habbo.fr";
            if ("fi".equals(h)) return "www.habbo.fi";
            if ("it".equals(h)) return "www.habbo.it";
            if ("nl".equals(h)) return "www.habbo.nl";
            if ("tr".equals(h)) return "www.habbo.com.tr";
            return "www.habbo.com.br";
        }
    }


    private class CircularPullProgressView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float progressPct = 0f;

        CircularPullProgressView(Context context) {
            super(context);
        }

        void setProgressPct(float value) {
            progressPct = Math.max(0f, Math.min(1f, value));
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float m = Math.min(w, h);
            if (m <= 0f) return;
            float cx = w / 2f;
            float cy = h / 2f;
            float stroke = Math.max(dp(4), m * 0.14f);
            float r = (m - stroke) / 2f - dp(1);
            RectF arc = new RectF(cx - r, cy - r, cx + r, cy + r);

            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(stroke);
            p.setColor(lightTheme ? Color.rgb(228, 220, 238) : Color.argb(90, 255, 255, 255));
            canvas.drawCircle(cx, cy, r, p);

            if (progressPct > 0f) {
                p.setShader(null);
                p.setColor(purple);
                canvas.drawArc(arc, -90f, 360f * progressPct, false, p);
            }
        }
    }

    private class SponsorHeadGlowView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        SponsorHeadGlowView(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            postInvalidateOnAnimation();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            if (w <= 0f || h <= 0f) return;
            // A fase vem do mesmo relógio para todos os itens: os brilhos ficam
            // perfeitamente juntos mesmo quando um head é criado depois dos outros.
            float phase = (SystemClock.uptimeMillis() % SPONSOR_GLOW_CYCLE_MS)
                    / (float)SPONSOR_GLOW_CYCLE_MS;
            float pulse = .5f - .5f * (float)Math.cos(phase * Math.PI * 2f);
            float size = Math.min(w, h) - dp(8);
            float cx = w / 2f;
            float cy = h / 2f;
            float radius = Math.max(1f, size / 2f);
            RectF r = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
            int first = Color.rgb(71, 29, 126);
            int middle = Color.rgb(134, 63, 213);
            int last = Color.rgb(74, 168, 228);

            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(112, 160, 78, 255));
            p.setShadowLayer(dp(7) + dp(2) * pulse, 0, dp(2), p.getColor());
            canvas.drawCircle(cx, cy, radius, p);
            p.clearShadowLayer();

            float shift = (phase - .5f) * r.width() * .35f;
            p.setShader(new LinearGradient(
                    r.left + shift,
                    r.top,
                    r.right + shift,
                    r.bottom,
                    new int[]{first, middle, last},
                    new float[]{0f, .55f, 1f},
                    Shader.TileMode.CLAMP
            ));
            canvas.drawCircle(cx, cy, radius, p);
            p.setShader(null);

            p.setShader(new RadialGradient(
                    r.left + r.width() * (.25f + .55f * phase),
                    r.top + r.height() * .18f,
                    r.width() * .86f,
                    new int[]{Color.argb(95,255,255,255), Color.argb(18,255,255,255), Color.TRANSPARENT},
                    new float[]{0f, .36f, 1f},
                    Shader.TileMode.CLAMP
            ));
            canvas.drawCircle(cx, cy, radius, p);
            p.setShader(null);

            canvas.save();
            Path clip = new Path();
            clip.addCircle(cx, cy, radius, Path.Direction.CW);
            canvas.clipPath(clip);
            float shimmerX = r.left - r.width() * .55f + phase * r.width() * 2.1f;
            p.setShader(new LinearGradient(
                    shimmerX - dp(14),
                    r.top,
                    shimmerX + dp(14),
                    r.bottom,
                    new int[]{Color.TRANSPARENT, Color.argb(76,255,255,255), Color.TRANSPARENT},
                    new float[]{0f, .5f, 1f},
                    Shader.TileMode.CLAMP
            ));
            canvas.drawRect(r, p);
            p.setShader(null);
            canvas.restore();

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(110, 245, 222, 255));
            canvas.drawCircle(cx, cy, Math.max(1f, radius - 1f), p);

            p.setStyle(Paint.Style.FILL);
            float blink = .45f + .55f * pulse;
            p.setColor(Color.argb((int)(185 * blink), 255, 255, 255));
            canvas.drawCircle(r.right - dp(8), r.top + dp(9), dp(2), p);
            canvas.drawCircle(r.left + dp(9), r.bottom - dp(10), dp(1), p);
            if (isAttachedToWindow() && isShown()) postInvalidateOnAnimation();
        }
    }

    public class HotelFlagDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        String hotel;
        boolean showBorder;
        HotelFlagDrawable(String hotelKey) { this(hotelKey, true); }
        HotelFlagDrawable(String hotelKey, boolean border) {
            hotel = normalizeHotelKey(hotelKey);
            if (hotel.isEmpty()) hotel = "br";
            showBorder = border;
        }
        @Override public int getIntrinsicWidth() { return dp(24); }
        @Override public int getIntrinsicHeight() { return dp(16); }
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            RectF r = new RectF(b.left, b.top, b.right, b.bottom);
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            c.save();
            Path clip = new Path();
            clip.addRoundRect(r, dp(3), dp(3), Path.Direction.CW);
            c.clipPath(clip);
            float w = r.width(), h = r.height(), x = r.left, y = r.top;
            if ("br".equals(hotel)) {
                p.setColor(Color.rgb(34, 166, 74)); c.drawRect(r, p);
                p.setColor(Color.rgb(255, 223, 64));
                Path d = new Path(); d.moveTo(x+w*.50f,y+h*.10f); d.lineTo(x+w*.90f,y+h*.50f); d.lineTo(x+w*.50f,y+h*.90f); d.lineTo(x+w*.10f,y+h*.50f); d.close(); c.drawPath(d,p);
                p.setColor(Color.rgb(39, 74, 160)); c.drawCircle(x+w*.50f, y+h*.50f, Math.min(w,h)*.20f, p);
            } else if ("com".equals(hotel)) {
                for (int i=0;i<7;i++){ p.setColor(i%2==0?Color.rgb(188,10,48):Color.WHITE); c.drawRect(x, y+h*i/7f, x+w, y+h*(i+1)/7f, p); }
                p.setColor(Color.rgb(40,60,130)); c.drawRect(x,y,x+w*.42f,y+h*.54f,p);
            } else if ("es".equals(hotel)) {
                p.setColor(Color.rgb(198, 0, 43)); c.drawRect(r,p); p.setColor(Color.rgb(255, 206, 0)); c.drawRect(x,y+h*.25f,x+w,y+h*.75f,p);
            } else if ("de".equals(hotel)) {
                p.setColor(Color.BLACK); c.drawRect(x,y,x+w,y+h/3f,p); p.setColor(Color.rgb(221,0,0)); c.drawRect(x,y+h/3f,x+w,y+2*h/3f,p); p.setColor(Color.rgb(255,206,0)); c.drawRect(x,y+2*h/3f,x+w,y+h,p);
            } else if ("fr".equals(hotel)) {
                p.setColor(Color.rgb(0,35,149)); c.drawRect(x,y,x+w/3f,y+h,p); p.setColor(Color.WHITE); c.drawRect(x+w/3f,y,x+2*w/3f,y+h,p); p.setColor(Color.rgb(237,41,57)); c.drawRect(x+2*w/3f,y,x+w,y+h,p);
            } else if ("fi".equals(hotel)) {
                p.setColor(Color.WHITE); c.drawRect(r,p); p.setColor(Color.rgb(0,53,128)); c.drawRect(x+w*.30f,y,x+w*.46f,y+h,p); c.drawRect(x,y+h*.38f,x+w,y+h*.58f,p);
            } else if ("it".equals(hotel)) {
                p.setColor(Color.rgb(0,146,70)); c.drawRect(x,y,x+w/3f,y+h,p); p.setColor(Color.WHITE); c.drawRect(x+w/3f,y,x+2*w/3f,y+h,p); p.setColor(Color.rgb(206,43,55)); c.drawRect(x+2*w/3f,y,x+w,y+h,p);
            } else if ("nl".equals(hotel)) {
                p.setColor(Color.rgb(174,28,40)); c.drawRect(x,y,x+w,y+h/3f,p); p.setColor(Color.WHITE); c.drawRect(x,y+h/3f,x+w,y+2*h/3f,p); p.setColor(Color.rgb(33,70,139)); c.drawRect(x,y+2*h/3f,x+w,y+h,p);
            } else if ("tr".equals(hotel)) {
                p.setColor(Color.rgb(227,10,23)); c.drawRect(r,p); p.setColor(Color.WHITE); c.drawCircle(x+w*.43f,y+h*.50f,h*.25f,p); p.setColor(Color.rgb(227,10,23)); c.drawCircle(x+w*.50f,y+h*.50f,h*.20f,p); p.setColor(Color.WHITE); Path star=new Path(); float cx=x+w*.64f, cy=y+h*.50f, rr=h*.15f; for(int i=0;i<10;i++){ double a=-Math.PI/2+i*Math.PI/5; float rad=(i%2==0)?rr:rr*.42f; float px=cx+(float)Math.cos(a)*rad, py=cy+(float)Math.sin(a)*rad; if(i==0) star.moveTo(px,py); else star.lineTo(px,py);} star.close(); c.drawPath(star,p);
            }
            c.restore();
            if (showBorder) {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(dp(1));
                p.setColor(Color.argb(90,0,0,0));
                c.drawRoundRect(r, dp(3), dp(3), p);
            }
        }
        @Override public void setAlpha(int alpha) { p.setAlpha(alpha); }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { p.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    public class AddButtonDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float x = b.left, y = b.top, w = b.width(), h = b.height();
            float size = Math.min(w, h);
            float left = x + (w - size) / 2f;
            float top = y + (h - size) / 2f;
            RectF r = new RectF(left, top, left + size, top + size);
            p.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom, purple2, purple, Shader.TileMode.CLAMP));
            p.setStyle(Paint.Style.FILL);
            c.drawRoundRect(r, dp(7), dp(7), p);
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(85,255,255,255));
            c.drawRoundRect(new RectF(r.left+1, r.top+1, r.right-1, r.bottom-1), dp(7), dp(7), p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(Color.WHITE);
            float cx = r.centerX(), cy = r.centerY();
            float len = size * 0.22f;
            c.drawLine(cx - len, cy, cx + len, cy, p);
            c.drawLine(cx, cy - len, cx, cy + len, p);
        }
        @Override public void setAlpha(int alpha) { p.setAlpha(alpha); }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { p.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }





    public class BottomNavBarDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            RectF surface = new RectF(b.left + dp(1), b.top + dp(1), b.right - dp(1), b.bottom - dp(1));
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(lightTheme ? Color.rgb(255, 255, 255) : Color.rgb(17, 16, 24));
            c.drawRoundRect(surface, dp(22), dp(22), p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(lightTheme ? Color.rgb(221, 218, 229) : Color.rgb(56, 51, 72));
            c.drawRoundRect(surface, dp(22), dp(22), p);
        }

        @Override public void setAlpha(int alpha) { p.setAlpha(alpha); }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { p.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    public class BottomNavIconDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        String type;
        boolean selected;
        BottomNavIconDrawable(String type, boolean selected) { this.type = type == null ? "home" : type; this.selected = selected; }

        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float w = b.width(), h = b.height(), x = b.left, y = b.top, m = Math.min(w, h);
            float cx = b.centerX(), cy = b.centerY();
            int color = bottomNavIconColor(selected);

            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(2f, m * .085f));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(color);

            if ("home".equals(type)) {
                // Lupa vetorial: grossa, arredondada, proporcional aos outros ícones e sem círculo interno.
                float lensR = m * .215f;
                float lx = cx - m * .075f;
                float ly = cy - m * .070f;
                float stroke = Math.max(3.0f, m * .102f);

                p.setStyle(Paint.Style.STROKE);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeJoin(Paint.Join.ROUND);

                // Leve base escura para dar profundidade sem criar novo círculo interno.
                p.setStrokeWidth(stroke + Math.max(1.2f, m * .026f));
                p.setColor(Color.argb(selected ? 105 : 86, 32, 0, 72));
                c.drawCircle(lx, ly, lensR, p);
                c.drawLine(lx + lensR * .72f, ly + lensR * .72f, cx + m * .265f, cy + m * .265f, p);

                p.setStrokeWidth(stroke);
                p.setColor(color);
                c.drawCircle(lx, ly, lensR, p);
                // O cabo começa fora da borda da lente para não sobrepor as linhas.
                c.drawLine(lx + lensR * .82f, ly + lensR * .82f, cx + m * .265f, cy + m * .265f, p);
            } else if ("visuals".equals(type)) {
                // Ícone de camiseta minimalista para o provador de visuais.
                Path shirt = new Path();
                shirt.moveTo(x + w*.27f, y + h*.28f);
                shirt.lineTo(x + w*.39f, y + h*.20f);
                shirt.quadTo(x + w*.50f, y + h*.28f, x + w*.61f, y + h*.20f);
                shirt.lineTo(x + w*.73f, y + h*.28f);
                shirt.lineTo(x + w*.86f, y + h*.43f);
                shirt.lineTo(x + w*.75f, y + h*.55f);
                shirt.lineTo(x + w*.70f, y + h*.49f);
                shirt.lineTo(x + w*.70f, y + h*.80f);
                shirt.lineTo(x + w*.30f, y + h*.80f);
                shirt.lineTo(x + w*.30f, y + h*.49f);
                shirt.lineTo(x + w*.25f, y + h*.55f);
                shirt.lineTo(x + w*.14f, y + h*.43f);
                shirt.close();
                if (selected) {
                    p.setStyle(Paint.Style.FILL);
                    p.setColor(color);
                    c.drawPath(shirt, p);
                } else {
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(Math.max(2f, m * .075f));
                    p.setColor(color);
                    c.drawPath(shirt, p);
                }
            } else if ("heart".equals(type)) {
                Path heart = new Path();
                heart.moveTo(cx, cy + m*.27f);
                heart.cubicTo(cx - m*.40f, cy + m*.02f, cx - m*.34f, cy - m*.25f, cx - m*.16f, cy - m*.25f);
                heart.cubicTo(cx - m*.06f, cy - m*.25f, cx, cy - m*.17f, cx, cy - m*.12f);
                heart.cubicTo(cx, cy - m*.17f, cx + m*.06f, cy - m*.25f, cx + m*.16f, cy - m*.25f);
                heart.cubicTo(cx + m*.34f, cy - m*.25f, cx + m*.40f, cy + m*.02f, cx, cy + m*.27f);
                heart.close();
                if (selected) {
                    p.setStyle(Paint.Style.FILL);
                    p.setColor(color);
                    c.drawPath(heart, p);
                } else {
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(Math.max(2.1f, m * .078f));
                    p.setStrokeJoin(Paint.Join.ROUND);
                    p.setStrokeCap(Paint.Cap.ROUND);
                    p.setColor(color);
                    c.drawPath(heart, p);
                }
            } else {
                // Ícone tipo menu/hambúrguer minimalista, mais alto e proporcional aos outros.
                p.setStrokeWidth(Math.max(2.2f, m*.09f));
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setColor(color);
                float left = x + w*.17f;
                float right = x + w*.83f;
                c.drawLine(left, y + h*.27f, right, y + h*.27f, p);
                c.drawLine(left, y + h*.50f, right, y + h*.50f, p);
                c.drawLine(left, y + h*.73f, right, y + h*.73f, p);
            }
        }
        @Override public void setAlpha(int alpha) { p.setAlpha(alpha); }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { p.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }


    public class AchievementSwitchDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        boolean checked;
        AchievementSwitchDrawable(boolean checked) { this.checked = checked; }

        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float w = b.width(), h = b.height(), x = b.left, y = b.top;
            float pad = Math.max(1f, h * .08f);
            RectF track = new RectF(x + pad, y + pad, x + w - pad, y + h - pad);
            float radius = track.height() / 2f;

            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(checked ? Color.rgb(39, 174, 96) : (lightTheme ? Color.rgb(210, 210, 214) : Color.rgb(55, 55, 64)));
            c.drawRoundRect(track, radius, radius, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, h * .045f));
            p.setColor(checked ? Color.rgb(39, 174, 96) : (lightTheme ? Color.rgb(196, 196, 200) : Color.rgb(72, 72, 82)));
            c.drawRoundRect(track, radius, radius, p);

            float knobRadius = track.height() * .42f;
            float knobCx = checked ? (track.right - radius) : (track.left + radius);
            float knobCy = track.centerY();

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            c.drawCircle(knobCx, knobCy, knobRadius, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, h * .035f));
            p.setColor(checked ? Color.argb(35, 0, 0, 0) : (lightTheme ? Color.rgb(190,190,194) : Color.argb(80,255,255,255)));
            c.drawCircle(knobCx, knobCy, knobRadius, p);
        }

        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    private void drawRemovedTrash(Canvas c, Rect bounds, int color, float scaleFactor) {
        Paint trashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trashPaint.setStyle(Paint.Style.STROKE); trashPaint.setStrokeCap(Paint.Cap.ROUND); trashPaint.setStrokeJoin(Paint.Join.ROUND); trashPaint.setColor(color);
        float w=bounds.width(), h=bounds.height(); float scale=Math.min(w,h)*scaleFactor/512f; float ox=bounds.left+(w-512f*scale)/2f; float oy=bounds.top+(h-512f*scale)/2f;
        trashPaint.setStrokeWidth(Math.max(2.3f, 18f*scale));
        Path handle=new Path(); handle.moveTo(ox+210f*scale,oy+154f*scale); handle.lineTo(ox+210f*scale,oy+143f*scale); handle.cubicTo(ox+210f*scale,oy+134f*scale,ox+217f*scale,oy+127f*scale,ox+226f*scale,oy+127f*scale); handle.lineTo(ox+286f*scale,oy+127f*scale); handle.cubicTo(ox+295f*scale,oy+127f*scale,ox+302f*scale,oy+134f*scale,ox+302f*scale,oy+143f*scale); handle.lineTo(ox+302f*scale,oy+154f*scale); c.drawPath(handle,trashPaint);
        c.drawRoundRect(new RectF(ox+123f*scale,oy+154f*scale,ox+389f*scale,oy+184f*scale),15f*scale,15f*scale,trashPaint);
        Path body=new Path(); body.moveTo(ox+160f*scale,oy+194f*scale); body.lineTo(ox+160f*scale,oy+382f*scale); body.cubicTo(ox+160f*scale,oy+398f*scale,ox+173f*scale,oy+411f*scale,ox+189f*scale,oy+411f*scale); body.lineTo(ox+323f*scale,oy+411f*scale); body.cubicTo(ox+339f*scale,oy+411f*scale,ox+352f*scale,oy+398f*scale,ox+352f*scale,oy+382f*scale); body.lineTo(ox+352f*scale,oy+194f*scale); c.drawPath(body,trashPaint);
        c.drawLine(ox+212f*scale,oy+246f*scale,ox+212f*scale,oy+356f*scale,trashPaint); c.drawLine(ox+256f*scale,oy+246f*scale,ox+256f*scale,oy+356f*scale,trashPaint); c.drawLine(ox+300f*scale,oy+246f*scale,ox+300f*scale,oy+356f*scale,trashPaint);
    }

    public class TrashTabDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        boolean active;
        TrashTabDrawable(boolean active) { this.active = active; }
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            int color = active ? purple : (lightTheme ? Color.rgb(59,7,91) : Color.WHITE);
            drawRemovedTrash(c, b, color, .82f);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class FavoriteStarDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        boolean active;
        FavoriteStarDrawable(boolean active) { this.active = active; }

        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float cx = b.centerX(), cy = b.centerY(), m = Math.min(b.width(), b.height());
            int color = bottomNavIconColor(active);

            Path heart = new Path();
            heart.moveTo(cx, cy + m*.27f);
            heart.cubicTo(cx - m*.40f, cy + m*.02f, cx - m*.34f, cy - m*.25f, cx - m*.16f, cy - m*.25f);
            heart.cubicTo(cx - m*.06f, cy - m*.25f, cx, cy - m*.17f, cx, cy - m*.12f);
            heart.cubicTo(cx, cy - m*.17f, cx + m*.06f, cy - m*.25f, cx + m*.16f, cy - m*.25f);
            heart.cubicTo(cx + m*.34f, cy - m*.25f, cx + m*.40f, cy + m*.02f, cx, cy + m*.27f);
            heart.close();

            p.setShader(null);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(color);
            if (active) {
                p.setStyle(Paint.Style.FILL);
                c.drawPath(heart, p);
            } else {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(Math.max(2.1f, m * .078f));
                c.drawPath(heart, p);
            }
        }

        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }


    public class RemoveXDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float w=b.width(), h=b.height(), x=b.left, y=b.top, m=Math.min(w,h);
            RectF bg = new RectF(x+m*.10f, y+m*.10f, x+w-m*.10f, y+h-m*.10f);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(190, 48, 70));
            c.drawRoundRect(bg, m*.22f, m*.22f, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(Math.max(2f, m*.075f));
            p.setColor(Color.WHITE);
            c.drawLine(x+w*.35f, y+h*.35f, x+w*.65f, y+h*.65f, p);
            c.drawLine(x+w*.65f, y+h*.35f, x+w*.35f, y+h*.65f, p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class HistoryClockDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float w = b.width(), h = b.height(), x = b.left, y = b.top;
            float cx = x + w / 2f, cy = y + h / 2f;
            float radius = Math.min(w, h) * 0.28f;
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(lightTheme ? Color.argb(0,0,0,0) : Color.argb(0,255,255,255));
            c.drawRect(x, y, x+w, y+h, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(lightTheme ? Color.rgb(45,45,45) : Color.argb(235,255,255,255));
            RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
            c.drawArc(oval, 35, 285, false, p);

            Path arrow = new Path();
            double a = Math.toRadians(35);
            float ax = cx + (float)Math.cos(a) * radius;
            float ay = cy + (float)Math.sin(a) * radius;
            arrow.moveTo(ax, ay);
            arrow.lineTo(ax - dp(8), ay - dp(1));
            arrow.moveTo(ax, ay);
            arrow.lineTo(ax - dp(3), ay + dp(7));
            c.drawPath(arrow, p);

            c.drawLine(cx, cy, cx, cy - radius * 0.52f, p);
            c.drawLine(cx, cy, cx + radius * 0.48f, cy + radius * 0.18f, p);
            p.setStyle(Paint.Style.FILL);
            c.drawCircle(cx, cy, dp(2), p);
        }
        @Override public void setAlpha(int alpha) { p.setAlpha(alpha); }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { p.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    public class ArrowButtonDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); boolean left;
        ArrowButtonDrawable(boolean left){ this.left = left; }
        @Override public void draw(Canvas c) {
            Rect b = getBounds(); float w=b.width(), h=b.height(), x=b.left, y=b.top;
            RectF r = new RectF(x, y, x+w, y+h);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(38, 35, 45));
            c.drawRoundRect(r, dp(11), dp(11), p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(70,255,255,255));
            c.drawRoundRect(new RectF(x+1,y+1,x+w-1,y+h-1), dp(11), dp(11), p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(Color.WHITE);
            float yMid = y + h * .50f;
            float startX = x + w * .26f, endX = x + w * .74f;
            if (left) {
                c.drawLine(endX, yMid, startX, yMid, p);
                Path path = new Path();
                path.moveTo(x+w*.42f, y+h*.30f);
                path.lineTo(startX, yMid);
                path.lineTo(x+w*.42f, y+h*.70f);
                c.drawPath(path, p);
            } else {
                c.drawLine(startX, yMid, endX, yMid, p);
                Path path = new Path();
                path.moveTo(x+w*.58f, y+h*.30f);
                path.lineTo(endX, yMid);
                path.lineTo(x+w*.58f, y+h*.70f);
                c.drawPath(path, p);
            }
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class ShirtDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds(); float w=b.width(), h=b.height(), x=b.left, y=b.top;
            RectF r = new RectF(x, y, x+w, y+h);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(38, 35, 45));
            c.drawRoundRect(r, dp(11), dp(11), p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(70,255,255,255));
            c.drawRoundRect(new RectF(x+1,y+1,x+w-1,y+h-1), dp(11), dp(11), p);

            float ox = x + w * .18f, oy = y + h * .15f, sw = w * .64f, sh = h * .68f;
            Path leftSleeve = new Path();
            leftSleeve.moveTo(ox+sw*.22f, oy+sh*.10f); leftSleeve.lineTo(ox+sw*.02f, oy+sh*.22f); leftSleeve.lineTo(ox+sw*.15f, oy+sh*.42f); leftSleeve.lineTo(ox+sw*.33f, oy+sh*.28f); leftSleeve.close();
            Path rightSleeve = new Path();
            rightSleeve.moveTo(ox+sw*.78f, oy+sh*.10f); rightSleeve.lineTo(ox+sw*.98f, oy+sh*.22f); rightSleeve.lineTo(ox+sw*.85f, oy+sh*.42f); rightSleeve.lineTo(ox+sw*.67f, oy+sh*.28f); rightSleeve.close();
            p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(255,107,122)); c.drawPath(leftSleeve,p); c.drawPath(rightSleeve,p);
            Path body = new Path();
            body.moveTo(ox+sw*.34f, oy+sh*.10f); body.lineTo(ox+sw*.22f, oy+sh*.16f); body.lineTo(ox+sw*.22f, oy+sh*.38f); body.lineTo(ox+sw*.28f, oy+sh*.44f); body.lineTo(ox+sw*.28f, oy+sh*.95f); body.lineTo(ox+sw*.72f, oy+sh*.95f); body.lineTo(ox+sw*.72f, oy+sh*.44f); body.lineTo(ox+sw*.78f, oy+sh*.38f); body.lineTo(ox+sw*.78f, oy+sh*.16f); body.lineTo(ox+sw*.66f, oy+sh*.10f); body.close();
            p.setColor(Color.rgb(217,75,66)); c.drawPath(body,p);
            p.setColor(Color.rgb(182,58,51)); c.drawRect(ox+sw*.28f, oy+sh*.44f, ox+sw*.34f, oy+sh*.95f, p); c.drawRect(ox+sw*.66f, oy+sh*.44f, ox+sw*.72f, oy+sh*.95f, p);
            p.setColor(Color.rgb(255,107,122)); c.drawRect(ox+sw*.36f, oy+sh*.84f, ox+sw*.64f, oy+sh*.91f, p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class PlaceholderDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); String type;
        PlaceholderDrawable(String t){type=t;}
        @Override public void draw(Canvas c){ Rect b=getBounds(); p.setStyle(Paint.Style.FILL); p.setColor(Color.argb(32,255,255,255)); c.drawRoundRect(new RectF(b), dp(12), dp(12), p); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); p.setColor(Color.argb(190,255,255,255)); float cx=b.centerX(), cy=b.centerY(); if("groups".equals(type)){ c.drawCircle(cx,cy,Math.min(b.width(),b.height())*.25f,p); c.drawCircle(cx,cy,Math.min(b.width(),b.height())*.12f,p);} else { Path path=new Path(); path.moveTo(cx,b.top+dp(12)); path.lineTo(b.right-dp(12),cy-dp(4)); path.lineTo(b.right-dp(12),cy+dp(18)); path.lineTo(cx,b.bottom-dp(10)); path.lineTo(b.left+dp(12),cy+dp(18)); path.lineTo(b.left+dp(12),cy-dp(4)); path.close(); c.drawPath(path,p);} }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class IconView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); String type;
        public IconView(Context c, String t) { super(c); type = t; }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w=getWidth(), h=getHeight(), cx=w/2f, cy=h/2f, m=Math.min(w,h);
            p.setShader(null); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(2f, m*.11f)); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND); p.setColor(lightTheme ? Color.rgb(33, 33, 33) : Color.WHITE);
            if ("dot".equals(type)) { p.setStyle(Paint.Style.FILL); p.setColor(purple); c.drawCircle(cx,cy,m*.28f,p); return; }
            if ("lock".equals(type)) { RectF body = new RectF(cx-m*.26f, cy-m*.02f, cx+m*.26f, cy+m*.30f); c.drawRoundRect(body, m*.08f, m*.08f, p); c.drawArc(new RectF(cx-m*.22f, cy-m*.36f, cx+m*.22f, cy+m*.12f), 200, 140, false, p); return; }
            if ("ban".equals(type)) {
                p.setStrokeWidth(Math.max(2f, m*.105f));
                c.drawCircle(cx, cy, m*.34f, p);
                c.drawLine(cx-m*.23f, cy-m*.23f, cx+m*.23f, cy+m*.23f, p);
                return;
            }
            if ("status".equals(type)) { p.setColor(Color.rgb(255,120,135)); c.drawCircle(cx,cy,m*.34f,p); p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(240,40,54)); c.drawCircle(cx,cy,m*.18f,p); return; }
            if ("clock".equals(type)) { c.drawCircle(cx,cy,m*.36f,p); c.drawLine(cx,cy,cx,cy-m*.20f,p); c.drawLine(cx,cy,cx+m*.17f,cy+m*.11f,p); return; }
            if ("calendar".equals(type)) { RectF r=new RectF(w*.16f,h*.22f,w*.84f,h*.82f); c.drawRoundRect(r,m*.10f,m*.10f,p); c.drawLine(w*.16f,h*.42f,w*.84f,h*.42f,p); c.drawLine(w*.32f,h*.12f,w*.32f,h*.30f,p); c.drawLine(w*.68f,h*.12f,w*.68f,h*.30f,p); return; }
            if ("friends".equals(type)) { c.drawCircle(cx-m*.18f,cy-m*.08f,m*.11f,p); c.drawCircle(cx+m*.18f,cy-m*.08f,m*.11f,p); c.drawArc(new RectF(cx-m*.43f,cy+m*.05f, cx-m*.02f, cy+m*.46f),205,130,false,p); c.drawArc(new RectF(cx+m*.02f,cy+m*.05f, cx+m*.43f, cy+m*.46f),205,130,false,p); return; }
            if ("rooms".equals(type)) {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeJoin(Paint.Join.ROUND);
                float scale = Math.min(w, h) * 0.88f / 512f;
                float ox = (w - 512f * scale) / 2f;
                float oy = (h - 512f * scale) / 2f;
                p.setStrokeWidth(Math.max(2.8f, Math.min(w, h) * .118f));
                Path path = new Path();
                path.moveTo(ox + 256f * scale, oy + 18f * scale);
                path.lineTo(ox + 18f * scale, oy + 138f * scale);
                path.lineTo(ox + 18f * scale, oy + 398f * scale);
                path.lineTo(ox + 256f * scale, oy + 494f * scale);
                path.lineTo(ox + 494f * scale, oy + 398f * scale);
                path.lineTo(ox + 494f * scale, oy + 138f * scale);
                path.close();
                path.moveTo(ox + 256f * scale, oy + 18f * scale);
                path.lineTo(ox + 256f * scale, oy + 300f * scale);
                path.moveTo(ox + 18f * scale, oy + 398f * scale);
                path.lineTo(ox + 256f * scale, oy + 300f * scale);
                path.moveTo(ox + 256f * scale, oy + 300f * scale);
                path.lineTo(ox + 494f * scale, oy + 398f * scale);
                c.drawPath(path, p);
                return;
            }
            if ("groups".equals(type)) { c.drawCircle(cx,cy,m*.36f,p); c.drawCircle(cx,cy,m*.17f,p); Path chk=new Path(); chk.moveTo(cx-m*.10f,cy); chk.lineTo(cx-m*.02f,cy+m*.09f); chk.lineTo(cx+m*.15f,cy-m*.11f); c.drawPath(chk,p); return; }
            if ("photos".equals(type)) { RectF r=new RectF(w*.16f,h*.22f,w*.84f,h*.78f); c.drawRoundRect(r,m*.09f,m*.09f,p); c.drawCircle(w*.32f,h*.38f,m*.06f,p); c.drawLine(w*.22f,h*.68f,w*.43f,h*.52f,p); c.drawLine(w*.43f,h*.52f,w*.78f,h*.68f,p); return; }
            if ("star".equals(type)) { Path path=new Path(); for(int i=0;i<10;i++){ double a=-Math.PI/2+i*Math.PI/5; float rr=(i%2==0)?m*.40f:m*.17f; float x=cx+(float)Math.cos(a)*rr, y=cy+(float)Math.sin(a)*rr; if(i==0) path.moveTo(x,y); else path.lineTo(x,y);} path.close(); c.drawPath(path,p); return; }
            if ("badge".equals(type)) {
                p.setShader(null);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(Math.max(2f, m*.075f));
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setColor(lightTheme ? Color.rgb(33, 33, 33) : Color.WHITE);

                for (int i=0;i<8;i++){
                    double a = -Math.PI/2 + i*Math.PI/4;
                    float px = cx + (float)Math.cos(a)*m*.20f;
                    float py = cy + (float)Math.sin(a)*m*.20f;
                    RectF petalOval = new RectF(px-m*.11f, py-m*.17f, px+m*.11f, py+m*.17f);
                    c.save();
                    c.rotate((float)Math.toDegrees(a)+90, px, py);
                    c.drawOval(petalOval, p);
                    c.restore();
                }
                p.setStyle(Paint.Style.FILL);
                p.setColor(lightTheme ? Color.rgb(33, 33, 33) : Color.WHITE);
                c.drawCircle(cx, cy, m*.085f, p);
                return;
            }
            if ("level".equals(type)) { p.setStyle(Paint.Style.FILL); Path path=new Path(); path.moveTo(cx,h*.16f); path.lineTo(w*.80f,h*.48f); path.lineTo(w*.62f,h*.48f); path.lineTo(w*.62f,h*.84f); path.lineTo(w*.38f,h*.84f); path.lineTo(w*.38f,h*.48f); path.lineTo(w*.20f,h*.48f); path.close(); c.drawPath(path,p); }
        }
    }


    public class NoAdsBannerDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            RectF r = new RectF(b.left + dp(1), b.top + dp(1), b.right - dp(1), b.bottom - dp(1));
            p.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom,
                    new int[]{Color.rgb(133,83,235), Color.rgb(132,52,217), Color.rgb(68,36,179)},
                    new float[]{0f,.52f,1f}, Shader.TileMode.CLAMP));
            p.setStyle(Paint.Style.FILL);
            c.drawRoundRect(r, dp(18), dp(18), p);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(24,255,255,255));
            c.drawCircle(r.left + r.height()*.94f, r.top + r.height()*.08f, r.height()*.42f, p);
            p.setColor(Color.argb(18,255,255,255));
            c.drawCircle(r.right - r.height()*.84f, r.bottom + r.height()*.04f, r.height()*.60f, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(105,255,255,255));
            c.drawRoundRect(new RectF(r.left+1, r.top+1, r.right-1, r.bottom-1), dp(18), dp(18), p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(130,255,255,255));
            c.drawCircle(r.left + r.height()*.62f, r.top + r.height()*.24f, r.height()*.018f, p);
            c.drawCircle(r.left + r.height()*.92f, r.top + r.height()*.12f, r.height()*.022f, p);
            c.drawCircle(r.left + r.height()*.36f, r.bottom - r.height()*.22f, r.height()*.016f, p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class PremiumCrownDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b=getBounds(); float cx=b.centerX(), cy=b.centerY(), m=Math.min(b.width(),b.height());
            p.setStyle(Paint.Style.FILL); p.setColor(Color.WHITE); c.drawCircle(cx,cy,m*.46f,p);
            p.setColor(Color.rgb(255,190,0));
            Path crown=new Path(); crown.moveTo(cx-m*.28f,cy+m*.13f); crown.lineTo(cx-m*.33f,cy-m*.15f); crown.lineTo(cx-m*.12f,cy-m*.04f); crown.lineTo(cx,cy-m*.26f); crown.lineTo(cx+m*.12f,cy-m*.04f); crown.lineTo(cx+m*.33f,cy-m*.15f); crown.lineTo(cx+m*.28f,cy+m*.13f); crown.close(); c.drawPath(crown,p);
            c.drawRoundRect(new RectF(cx-m*.27f,cy+m*.17f,cx+m*.27f,cy+m*.25f),m*.025f,m*.025f,p); c.drawCircle(cx-m*.33f,cy-m*.16f,m*.035f,p); c.drawCircle(cx,cy-m*.27f,m*.035f,p); c.drawCircle(cx+m*.33f,cy-m*.16f,m*.035f,p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class TinyNoAdDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b=getBounds(); float cx=b.centerX(), cy=b.centerY(), m=Math.min(b.width(),b.height());
            p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND); p.setStrokeWidth(Math.max(1.3f,m*.075f)); p.setColor(Color.rgb(255,193,24));
            RectF r=new RectF(cx-m*.30f,cy-m*.22f,cx+m*.30f,cy+m*.22f); c.drawRoundRect(r,m*.07f,m*.07f,p); p.setTextAlign(Paint.Align.CENTER); p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD)); String adLabel=t(R.string.ad_badge); p.setTextSize(m*(adLabel.length()>2 ? .16f : .20f)); p.setStyle(Paint.Style.FILL); c.drawText(adLabel,cx,cy+m*.075f,p); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(1.6f,m*.085f)); c.drawLine(cx-m*.38f,cy+m*.34f,cx+m*.38f,cy-m*.34f,p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class PremiumArrowDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b=getBounds(); float cx=b.centerX(), cy=b.centerY(), m=Math.min(b.width(),b.height());
            p.setShader(new LinearGradient(b.left,b.top,b.right,b.bottom,Color.rgb(185,82,255),Color.rgb(119,65,236),Shader.TileMode.CLAMP)); p.setStyle(Paint.Style.FILL); c.drawCircle(cx,cy,m*.46f,p); p.setShader(null);
            p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND); p.setStrokeWidth(Math.max(2.4f,m*.08f)); p.setColor(Color.WHITE); Path a=new Path(); a.moveTo(cx-m*.09f,cy-m*.19f); a.lineTo(cx+m*.12f,cy); a.lineTo(cx-m*.09f,cy+m*.19f); c.drawPath(a,p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class SupporterProfileButtonDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            float size = Math.min(bounds.width(), bounds.height());
            float cx = bounds.centerX();
            float cy = bounds.centerY();
            RectF background = new RectF(
                    bounds.left + size * .08f,
                    bounds.top + size * .08f,
                    bounds.right - size * .08f,
                    bounds.bottom - size * .08f
            );
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(
                    background.left,
                    background.top,
                    background.right,
                    background.bottom,
                    Color.rgb(76, 29, 149),
                    Color.rgb(168, 85, 247),
                    Shader.TileMode.CLAMP
            ));
            canvas.drawRoundRect(background, size * .25f, size * .25f, p);
            p.setShader(null);
            p.setShader(new RadialGradient(
                    background.right - size * .12f,
                    background.top + size * .10f,
                    size * .55f,
                    Color.argb(105,255,255,255),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
            ));
            canvas.drawRoundRect(background, size * .25f, size * .25f, p);
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, size * .035f));
            p.setColor(Color.argb(105,255,255,255));
            canvas.drawRoundRect(background, size * .25f, size * .25f, p);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            canvas.drawCircle(cx, cy - size * .10f, size * .105f, p);
            RectF shoulders = new RectF(
                    cx - size * .20f,
                    cy + size * .035f,
                    cx + size * .20f,
                    cy + size * .27f
            );
            canvas.drawRoundRect(shoulders, size * .12f, size * .12f, p);

            // Brilho roxo discreto no lugar da antiga coroa dourada.
            p.setColor(Color.rgb(238, 214, 255));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1.5f, size * .045f));
            p.setStrokeCap(Paint.Cap.ROUND);
            float sx = background.right - size * .13f;
            float sy = background.top + size * .15f;
            float ray = size * .075f;
            canvas.drawLine(sx - ray, sy, sx + ray, sy, p);
            canvas.drawLine(sx, sy - ray, sx, sy + ray, p);
        }
        @Override public void setAlpha(int alpha){p.setAlpha(alpha);}
        @Override public void setColorFilter(android.graphics.ColorFilter filter){p.setColorFilter(filter);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class RewardVideoDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float w = b.width(), h = b.height(), cx = b.centerX(), cy = b.centerY(), m = Math.min(w, h);
            RectF bgRect = new RectF(b.left + m*.10f, b.top + m*.10f, b.right - m*.10f, b.bottom - m*.10f);
            p.setShader(new LinearGradient(bgRect.left, bgRect.top, bgRect.right, bgRect.bottom, purple2, purple, Shader.TileMode.CLAMP));
            p.setStyle(Paint.Style.FILL);
            c.drawRoundRect(bgRect, m*.24f, m*.24f, p);
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, m*.035f));
            p.setColor(Color.argb(80,255,255,255));
            c.drawRoundRect(bgRect, m*.24f, m*.24f, p);

            RectF screenRect = new RectF(cx-m*.25f, cy-m*.17f, cx+m*.25f, cy+m*.17f);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(2f, m*.06f));
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(Color.WHITE);
            c.drawRoundRect(screenRect, m*.06f, m*.06f, p);

            Path play = new Path();
            play.moveTo(cx-m*.055f, cy-m*.080f);
            play.lineTo(cx-m*.055f, cy+m*.080f);
            play.lineTo(cx+m*.100f, cy);
            play.close();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            c.drawPath(play, p);

            p.setStrokeWidth(Math.max(1.5f, m*.035f));
            c.drawLine(cx-m*.09f, cy+m*.26f, cx+m*.09f, cy+m*.26f, p);
            c.drawLine(cx, cy+m*.16f, cx, cy+m*.26f, p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }




    public class VisualSaveLookDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float m = Math.min(b.width(), b.height());
            float cx = b.centerX(), cy = b.centerY();
            int iconColor = lightTheme ? Color.rgb(42,42,46) : Color.argb(242,255,255,255);
            int cutColor = lightTheme ? Color.WHITE : bg;

            p.setShader(null);
            p.setAntiAlias(true);
            p.setStyle(Paint.Style.FILL);
            p.setColor(iconColor);

            RectF body = new RectF(cx - m * .34f, cy - m * .35f, cx + m * .34f, cy + m * .35f);
            float r = m * .085f;
            Path disk = new Path();
            disk.moveTo(body.left + r, body.top);
            disk.lineTo(body.right - m * .16f, body.top);
            disk.lineTo(body.right, body.top + m * .16f);
            disk.lineTo(body.right, body.bottom - r);
            disk.quadTo(body.right, body.bottom, body.right - r, body.bottom);
            disk.lineTo(body.left + r, body.bottom);
            disk.quadTo(body.left, body.bottom, body.left, body.bottom - r);
            disk.lineTo(body.left, body.top + r);
            disk.quadTo(body.left, body.top, body.left + r, body.top);
            disk.close();
            c.drawPath(disk, p);

            p.setColor(cutColor);
            RectF topSlot = new RectF(cx - m * .20f, cy - m * .27f, cx + m * .21f, cy - m * .10f);
            c.drawRoundRect(topSlot, m * .04f, m * .04f, p);

            p.setColor(iconColor);
            RectF topMark = new RectF(cx + m * .10f, cy - m * .245f, cx + m * .15f, cy - m * .125f);
            c.drawRoundRect(topMark, m * .02f, m * .02f, p);

            p.setColor(cutColor);
            c.drawCircle(cx, cy + m * .16f, m * .115f, p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class VisualSavedLooksDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float m = Math.min(b.width(), b.height());
            float cx = b.centerX(), cy = b.centerY();
            int iconColor = lightTheme ? Color.rgb(42,42,46) : Color.argb(242,255,255,255);

            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeWidth(Math.max(dp(2), m * 0.105f));
            p.setColor(iconColor);

            Path hook = new Path();
            hook.moveTo(cx, cy - m * .31f);
            hook.cubicTo(cx + m * .20f, cy - m * .32f, cx + m * .20f, cy - m * .08f, cx + m * .035f, cy - m * .055f);
            c.drawPath(hook, p);

            c.drawLine(cx, cy - m * .04f, cx, cy + m * .10f, p);

            Path hanger = new Path();
            hanger.moveTo(cx, cy + m * .10f);
            hanger.lineTo(cx - m * .36f, cy + m * .38f);
            hanger.quadTo(cx - m * .40f, cy + m * .42f, cx - m * .31f, cy + m * .42f);
            hanger.lineTo(cx + m * .31f, cy + m * .42f);
            hanger.quadTo(cx + m * .40f, cy + m * .42f, cx + m * .36f, cy + m * .38f);
            hanger.close();
            c.drawPath(hanger, p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class ThemeIconButtonDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        boolean sun;
        boolean selected;
        ThemeIconButtonDrawable(boolean sun, boolean selected) {
            this.sun = sun;
            this.selected = selected;
        }
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            RectF r = new RectF(b.left + dp(1), b.top + dp(1), b.right - dp(1), b.bottom - dp(1));
            float rad = dp(14);

            p.setStyle(Paint.Style.FILL);
            if (selected) {
                LinearGradient g = new LinearGradient(r.left, r.top, r.right, r.bottom, purple2, purple, Shader.TileMode.CLAMP);
                p.setShader(g);
                c.drawRoundRect(r, rad, rad, p);
                p.setShader(null);
            } else {
                p.setColor(lightTheme ? Color.rgb(250,250,250) : Color.argb(34, 255,255,255));
                c.drawRoundRect(r, rad, rad, p);
            }

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(selected ? Color.argb(120,255,255,255) : (lightTheme ? Color.rgb(218,218,218) : Color.argb(30,255,255,255)));
            c.drawRoundRect(r, rad, rad, p);

            float cx = r.centerX();
            float cy = r.centerY();
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(selected ? Color.WHITE : (lightTheme ? Color.rgb(54,54,62) : Color.argb(220,255,255,255)));

            if (sun) {
                p.setStyle(Paint.Style.FILL);
                c.drawCircle(cx, cy, (dp(5) + Math.max(1, dp(1) / 5)), p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((dp(1) + Math.max(1, dp(1) / 2)));
                for (int i=0; i<8; i++) {
                    double a = i * Math.PI / 4.0;
                    float x1 = cx + (float)Math.cos(a) * dp(10);
                    float y1 = cy + (float)Math.sin(a) * dp(10);
                    float x2 = cx + (float)Math.cos(a) * dp(14);
                    float y2 = cy + (float)Math.sin(a) * dp(14);
                    c.drawLine(x1, y1, x2, y2, p);
                }
            } else {
                p.setStyle(Paint.Style.FILL);
                c.drawCircle(cx - dp(1), cy + dp(1), (dp(10) + Math.max(1, dp(1) / 2)), p);
                p.setColor(selected ? purple : (lightTheme ? Color.rgb(250,250,250) : Color.rgb(40, 28, 54)));
                c.drawCircle(cx + dp(4), cy - dp(3), (dp(10) + Math.max(1, dp(1) / 5)), p);
            }
        }
        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class TutorialOverlayDrawable extends Drawable {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final FrameLayout host;
        private final View target;
        private final int paddingDp;
        private final int step;
        private float pulse = 0f;

        TutorialOverlayDrawable(FrameLayout overlayHost, View targetView, int padding, int s) {
            host = overlayHost;
            target = targetView;
            paddingDp = padding;
            step = s;
        }

        void setPulse(float value) {
            pulse = Math.max(0f, Math.min(1f, value));
            invalidateSelf();
        }

        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            RectF hole = tutorialTargetBounds(host, target, paddingDp);
            boolean hasHole = hole != null;
            float radius = step == 1 ? dp(26) : dp(24);
            int accent = tutorialAccentColor(step);
            int secondary = tutorialAccentSecondaryColor(step);

            Path overlayPath = new Path();
            overlayPath.setFillType(Path.FillType.EVEN_ODD);
            overlayPath.addRect(new RectF(b.left, b.top, b.right, b.bottom), Path.Direction.CW);
            if (hasHole) overlayPath.addRoundRect(hole, radius, radius, Path.Direction.CW);

            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(
                    b.left,
                    b.top,
                    b.right,
                    b.bottom,
                    Color.argb(222, 3, 3, 8),
                    Color.argb(232, 13, 5, 21),
                    Shader.TileMode.CLAMP
            ));
            c.drawPath(overlayPath, p);
            p.setShader(null);
            if (!hasHole) return;

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(8) + (dp(7) * pulse));
            p.setColor(Color.argb(
                    (int) (70 - (30 * pulse)),
                    Color.red(accent),
                    Color.green(accent),
                    Color.blue(accent)
            ));
            RectF halo = new RectF(
                    hole.left - dp(2),
                    hole.top - dp(2),
                    hole.right + dp(2),
                    hole.bottom + dp(2)
            );
            c.drawRoundRect(halo, radius + dp(5), radius + dp(5), p);

            p.setStrokeWidth(dp(2));
            p.setShader(new LinearGradient(
                    hole.left,
                    hole.top,
                    hole.right,
                    hole.bottom,
                    secondary,
                    accent,
                    Shader.TileMode.CLAMP
            ));
            c.drawRoundRect(hole, radius, radius, p);
            p.setShader(null);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            float dotRadius = dp(2) + (dp(1) * pulse);
            c.drawCircle(hole.left + dp(8), hole.top + dp(8), dotRadius, p);
            c.drawCircle(hole.right - dp(8), hole.bottom - dp(8), dotRadius, p);
        }

        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class ProfileTutorialOverlayDrawable extends Drawable {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final FrameLayout host;
        private final View target;
        private final int paddingDp;
        private final int step;
        private float pulse = 0f;

        ProfileTutorialOverlayDrawable(
                FrameLayout overlayHost,
                View targetView,
                int padding,
                int tutorialStep
        ) {
            host = overlayHost;
            target = targetView;
            paddingDp = padding;
            step = tutorialStep;
        }

        void setPulse(float value) {
            pulse = Math.max(0f, Math.min(1f, value));
            invalidateSelf();
        }

        @Override public void draw(Canvas c) {
            Rect bounds = getBounds();
            RectF hole = tutorialTargetBounds(host, target, paddingDp);
            boolean hasHole = hole != null;
            float radius = step == 3 ? dp(18) : dp(24);
            int accent = tutorialAccentColor(step);
            int secondary = tutorialAccentSecondaryColor(step);

            Path overlayPath = new Path();
            overlayPath.setFillType(Path.FillType.EVEN_ODD);
            overlayPath.addRect(new RectF(bounds.left, bounds.top, bounds.right, bounds.bottom), Path.Direction.CW);
            if (hasHole) overlayPath.addRoundRect(hole, radius, radius, Path.Direction.CW);

            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(
                    bounds.left,
                    bounds.top,
                    bounds.right,
                    bounds.bottom,
                    Color.argb(222, 3, 3, 8),
                    Color.argb(232, 13, 5, 21),
                    Shader.TileMode.CLAMP
            ));
            c.drawPath(overlayPath, p);
            p.setShader(null);
            if (!hasHole) return;

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(8) + (dp(7) * pulse));
            p.setColor(Color.argb(
                    (int) (70 - (30 * pulse)),
                    Color.red(accent),
                    Color.green(accent),
                    Color.blue(accent)
            ));
            RectF halo = new RectF(hole.left - dp(2), hole.top - dp(2), hole.right + dp(2), hole.bottom + dp(2));
            c.drawRoundRect(halo, radius + dp(5), radius + dp(5), p);

            p.setStrokeWidth(dp(2));
            p.setShader(new LinearGradient(hole.left, hole.top, hole.right, hole.bottom, secondary, accent, Shader.TileMode.CLAMP));
            c.drawRoundRect(hole, radius, radius, p);
            p.setShader(null);
        }

        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class TutorialCardDrawable extends Drawable {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int step;

        TutorialCardDrawable(int s) { step = s; }

        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            RectF r = new RectF(b.left + dp(3), b.top + dp(3), b.right - dp(3), b.bottom - dp(3));
            float radius = dp(23);
            int accent = tutorialAccentColor(step);
            int secondary = tutorialAccentSecondaryColor(step);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(18, 16, 25));
            p.setShadowLayer(dp(20), 0, dp(10), Color.argb(190, 0, 0, 0));
            c.drawRoundRect(r, radius, radius, p);
            p.clearShadowLayer();

            p.setShader(new RadialGradient(
                    r.left + dp(34),
                    r.bottom - dp(12),
                    Math.max(dp(170), r.width() * .86f),
                    new int[]{
                            Color.argb(82, Color.red(secondary), Color.green(secondary), Color.blue(secondary)),
                            Color.argb(18, Color.red(accent), Color.green(accent), Color.blue(accent)),
                            Color.TRANSPARENT
                    },
                    new float[]{0f, .48f, 1f},
                    Shader.TileMode.CLAMP
            ));
            c.drawRoundRect(r, radius, radius, p);
            p.setShader(null);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(74, 255, 255, 255));
            c.drawRoundRect(r, radius, radius, p);

            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(r.left, r.top, r.left, r.bottom, accent, secondary, Shader.TileMode.CLAMP));
            RectF accentBar = new RectF(r.left, r.top + dp(22), r.left + dp(4), r.bottom - dp(22));
            c.drawRoundRect(accentBar, dp(999), dp(999), p);
            p.setShader(null);
        }

        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

}
