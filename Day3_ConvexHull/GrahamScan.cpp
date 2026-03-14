#include <SFML/Graphics.hpp>
#include <cmath>
using namespace std;

template <typename T>
class Array
{
    T *data;
    size_t capacity;
    size_t sz;

    void grow()
    {
        capacity = capacity == 0 ? 1 : capacity * 2;

        T *newData = new T[capacity];

        for (size_t i = 0; i < sz; i++)
        {
            newData[i] = data[i];
        }
        delete[] data;
        data = newData;
    }

public:
    Array() : data(nullptr), capacity(0), sz(0) {}
    ~Array() { delete[] data; }

    void push_back(T val)
    {
        if (sz == capacity)
        {
            grow();
        }

        data[sz++] = val;
    }

    void pop_back()
    {
        if (sz > 0)
            sz--;
    }

    void clear() { sz = 0; }

    size_t size() const { return sz; }

    T &operator[](size_t i) { return data[i]; }

    T back() { return data[sz - 1]; }

    T *begin() { return data; }
};

struct Point
{
    float x, y;
};

Point pivot;
float distance(Point a, Point b)
{
    return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
}

int orientation(Point p, Point q, Point r)
{
    float val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);

    if (abs(val) < 1e-6)
    {
        return 0;
    }
    return val > 0 ? 1 : 2;
}

void quickSort(Point *arr, int low, int high)
{
    if (low >= high)
        return;

    Point pvt = arr[high];

    int i = low - 1;

    for (int j = low; j < high; j++)
    {
        int o = orientation(pivot, arr[j], pvt);

        if (o == 1 || (o == 0 && distance(pivot, arr[j]) < distance(pivot, pvt)))
        {
            i++;

            Point t = arr[i];
            arr[i] = arr[j];
            arr[j] = t;
        }
    }

    Point t = arr[i + 1];
    arr[i + 1] = arr[high];
    arr[high] = t;

    int pi = i + 1;

    quickSort(arr, low, pi - 1);
    quickSort(arr, pi + 1, high);
}

int main()
{
    sf::Font font("arial.ttf");
    sf::RenderWindow window(sf::VideoMode({900, 750}), "Graham Scan Visualizer");

    window.setFramerateLimit(60);

    Array<Point> points;
    Array<Point> hull;

    bool isRunning = false;
    bool showHullText = false;
    size_t currentIdx = 0;

    sf::Clock timer;

    while (window.isOpen())
    {
        while (const optional event = window.pollEvent())
        {
            if (event->is<sf::Event::Closed>())
                window.close();

            if (const auto *click = event->getIf<sf::Event::MouseButtonPressed>())
            {
                if (showHullText)
                {
                    showHullText = false;
                }
                sf::Vector2f mPos = {(float)click->position.x, (float)click->position.y};

                if (mPos.y > 680)
                {

                    if (mPos.x > 50 && mPos.x < 200)
                    {
                        if (points.size() >= 3 && !isRunning)
                        {
                            int ymin = 0;
                            for (int i = 1; i < points.size(); i++)
                            {
                                if (points[i].y > points[ymin].y || (points[i].y == points[ymin].y && points[i].x < points[ymin].x))
                                    ymin = i;
                            }
                            Point temp = points[0];
                            points[0] = points[ymin];
                            points[ymin] = temp;

                            pivot = points[0];
                            quickSort(points.begin(), 1, points.size() - 1);

                            hull.clear();
                            hull.push_back(points[0]);
                            currentIdx = 1;

                            isRunning = true;
                            timer.restart();
                        }
                        else if(points.size() < 3)
                        {
                            sf::Text errorText(font);
                            errorText.setString("Please add at least 3 points to run the algorithm.");
                            errorText.setCharacterSize(20);
                            errorText.setFillColor(sf::Color(200, 200, 200));
                            errorText.setOrigin(errorText.getLocalBounds().size / 2.f);
                            errorText.setPosition({650, 715});

                            window.draw(errorText);
                            window.display();
                            sf::sleep(sf::seconds(2));
                        }
                    }

                    else if (mPos.x > 250 && mPos.x < 400)
                    {
                        points.clear();
                        hull.clear();
                        isRunning = false;
                    }
                }

                else if (!isRunning)
                {
                    points.push_back({mPos.x, mPos.y});
                }
            }
        }
        if (isRunning && timer.getElapsedTime().asMilliseconds() > 2000)
        {
            if (currentIdx < points.size())
            {
                while (hull.size() > 1 &&
                       orientation(hull[hull.size() - 2], hull.back(), points[currentIdx]) != 1)
                {
                    hull.pop_back();
                }

                hull.push_back(points[currentIdx]);
                currentIdx++;
                timer.restart();
            }
            else
            {
                isRunning = false;
                showHullText = true;
            }
        }

        window.clear(sf::Color(20, 20, 20));
        sf::RectangleShape panel({900, 70});
        panel.setPosition({0, 680});
        panel.setFillColor(sf::Color(30, 30, 30));
        window.draw(panel);

        for (int x = 0; x < 900; x += 50)
        {
            sf::Vertex line[2];

            line[0].position = {(float)x, 0};
            line[0].color = sf::Color(40, 40, 40);

            line[1].position = {(float)x, 750};
            line[1].color = sf::Color(40, 40, 40);

            window.draw(line, 2, sf::PrimitiveType::Lines);
        }

        for (int y = 0; y < 750; y += 50)
        {
            sf::Vertex line[2];

            line[0].position = {0, (float)y};
            line[0].color = sf::Color(40, 40, 40);

            line[1].position = {900, (float)y};
            line[1].color = sf::Color(40, 40, 40);

            window.draw(line, 2, sf::PrimitiveType::Lines);
        }

        sf::RectangleShape runBtn({150, 50});
        runBtn.setPosition({50, 690});
        runBtn.setFillColor(isRunning ? sf::Color(80, 80, 80) : sf::Color(70, 130, 180));
        runBtn.setOutlineThickness(2);
        runBtn.setOutlineColor(sf::Color(120, 120, 120));

        window.draw(runBtn);

        sf::RectangleShape resetBtn({150, 50});
        resetBtn.setPosition({250, 690});
        resetBtn.setFillColor(sf::Color(160, 70, 70));
        resetBtn.setOutlineThickness(2);
        resetBtn.setOutlineColor(sf::Color(120, 120, 120));

        window.draw(resetBtn);

        sf::Text runText(font);
        runText.setString("RUN");
        runText.setCharacterSize(22);
        runText.setFillColor(sf::Color(240, 240, 240));
        runText.setPosition({95, 705});
        window.draw(runText);

        sf::Text resetText(font);
        resetText.setString("RESET");
        resetText.setCharacterSize(22);
        resetText.setFillColor(sf::Color(240, 240, 240));
        resetText.setOrigin(resetText.getLocalBounds().size / 2.f);
        resetText.setPosition({325, 710});
        window.draw(resetText);

        if (isRunning && currentIdx < points.size() && hull.size() > 0)
        {
            sf::Vertex scanningLine[2];
            scanningLine[0].position = {hull.back().x, hull.back().y};
            scanningLine[0].color = sf::Color::Yellow;
            scanningLine[1].position = {points[currentIdx].x, points[currentIdx].y};
            scanningLine[1].color = sf::Color::Yellow;

            window.draw(scanningLine, 2, sf::PrimitiveType::Lines);
        }

        if (hull.size() >= 2)
        {
            for (size_t i = 0; i < hull.size() - 1; i++)
            {
                sf::Vertex line[2];

                line[0].position = {hull[i].x, hull[i].y};
                line[0].color = sf::Color::Cyan;

                line[1].position = {hull[i + 1].x, hull[i + 1].y};
                line[1].color = sf::Color::Cyan;

                window.draw(line, 2, sf::PrimitiveType::Lines);
            }
        }

        if (!isRunning && hull.size() > 2)
        {
            sf::Vertex line[2];

            line[0].position = {hull.back().x, hull.back().y};
            line[0].color = sf::Color::Cyan;

            line[1].position = {hull[0].x, hull[0].y};
            line[1].color = sf::Color::Cyan;

            window.draw(line, 2, sf::PrimitiveType::Lines);
        }
        for (size_t i = 0; i < points.size(); i++)
        {
            sf::CircleShape dot(6);
            dot.setOrigin({6, 6});
            dot.setPosition({points[i].x, points[i].y});
            bool isInHull = false;
            for (size_t j = 0; j < hull.size(); j++)
            {
                if (points[i].x == hull[j].x && points[i].y == hull[j].y)
                {
                    isInHull = true;
                    break;
                }
            }

            if (isInHull)
                dot.setFillColor(sf::Color::Green);
            else if (isRunning && i == 0)
                dot.setFillColor(sf::Color::Yellow);
            else
                dot.setFillColor(sf::Color::White);

            window.draw(dot);
        }
        if (showHullText)
        {
            sf::Text hullText(font);
            hullText.setString("CONVEX HULL COMPLETED");
            hullText.setCharacterSize(50);
            hullText.setFillColor(sf::Color(200, 200, 200));
            hullText.setCharacterSize(20);

            hullText.setOrigin(hullText.getLocalBounds().size / 2.f);
            hullText.setPosition({600, 715});

            window.draw(hullText);
        }

        window.display();
    }

    return 0;
}