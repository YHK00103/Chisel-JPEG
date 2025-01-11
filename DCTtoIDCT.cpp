#include <iostream>
#include <cmath>

#define pi 3.14159265

using namespace std;

void DCT(int matrix[8][8], int result[8][8]) {
    for (int u = 0; u < 8; u++) {
        for (int v = 0; v < 8; v++) {
            float sum = 0;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) { 
                    float cosVal = cos((2 * i + 1) * u * pi / 16) * cos((2 * j + 1) * v * pi / 16) * 100;
                    sum += (matrix[i][j] - 128) * cosVal;
                }
            }

            int alphaU = (u == 0) ? floor((1.0 / sqrt(2)) * 100) : 100;
            int alphaV = (v == 0) ? floor((1.0 / sqrt(2)) * 100) : 100;

            result[u][v] = alphaU * alphaV * sum / 40000;
        }
    }

    cout << "DCT to IDCT" << endl;
    for (int i = 0; i < 8; i++) {
        cout << "{";
        for (int j = 0; j < 8; j++) {
            cout << result[i][j] << ", ";
        }
        cout << "}" << endl;
    }
    cout << endl;
}

void IDCT(int matrix[8][8]){
    int result[8][8];

    for(int i = 0; i < 8; i++){
        for(int j = 0; j < 8; j++){
            double sum = 0;
            for(int u = 0; u < 8; u++){
                for(int v = 0; v < 8; v++){
                    int alphaU = (u == 0) ? floor((1.0 / sqrt(2)) * 100) : 100;
                    int alphaV = (v == 0) ? floor((1.0 / sqrt(2)) * 100) : 100;

                    float cosVal = cos((2 * i + 1) * u * pi / 16) * cos((2 * j + 1) * v * pi / 16) * 100;
                    sum += alphaU * alphaV * cosVal * matrix[u][v];
                }
            }
            result[i][j] = sum / 40000 + 128;
        }
    }

    cout << "IDCT to DCT" << endl;
    for(int i = 0; i < 8; i++){
        cout << "{";
        for(int j = 0; j < 8; j++){
            cout << result[i][j] << " ";
        }
        cout << "}" << endl;
    }
    cout << endl;
    return;
}

int main() {
    int DCTmatrix[8][8] = {
        {62, 55, 55, 54, 49, 48, 47, 55},
        {62, 57, 54, 52, 48, 47, 48, 53},
        {61, 60, 52, 49, 48, 47, 49, 54},
        {63, 61, 60, 60, 63, 65, 68, 65},
        {67, 67, 70, 74, 79, 85, 91, 92},
        {82, 95, 101, 106, 114, 115, 112, 117},
        {96, 111, 115, 119, 128, 128, 130, 127},
        {109, 121, 127, 133, 139, 141, 140, 133}
    };

    int IDCTmatrix[8][8]; // 用來存放 DCT 的結果
    DCT(DCTmatrix, IDCTmatrix); // 傳入並修改 IDCTmatrix
    IDCT(IDCTmatrix);

    return 0;
}
